package com.smartteach.modules.resource.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

/**
 * Aliyun OSS backend. Uses OSS V1 signature (HMAC-SHA1). Java 8 compatible
 * (uses HttpURLConnection, no third-party SDK required).
 *
 * Bucket must allow public read on stored objects, or be fronted by a CDN.
 */
@Slf4j
public class OssFileStorage implements FileStorage {

    private static final String ALGO = "HmacSHA1";
    private static final DateTimeFormatter GMT = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            .withZone(ZoneOffset.UTC);

    private final FileStorageProperties props;

    public OssFileStorage(FileStorageProperties props) {
        this.props = props;
        validate();
    }

    private void validate() {
        FileStorageProperties.Oss c = props.getOss();
        if (isBlank(c.getEndpoint()) || isBlank(c.getBucket())
                || isBlank(c.getAccessKeyId()) || isBlank(c.getAccessKeySecret())) {
            throw new IllegalStateException(
                    "file.oss.{endpoint,bucket,access-key-id,access-key-secret} must be configured");
        }
    }

    @Override
    public String store(String keyPrefix, MultipartFile file) throws IOException {
        FileStorageProperties.Oss c = props.getOss();
        String fileName = file.getOriginalFilename() == null
                ? java.util.UUID.randomUUID().toString()
                : sanitizeName(file.getOriginalFilename());
        String key = (keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/") + fileName;

        byte[] body = file.getBytes();
        String md5 = md5Base64(body);
        String date = GMT.format(java.time.Instant.now());
        String contentType = file.getContentType() == null ? "" : file.getContentType();

        String stringToSign = "PUT\n" + md5 + "\n" + contentType + "\n" + date + "\n"
                + "/oss-api/"  // CanonicalizedOSSHeader (none here)
                + "/" + c.getBucket() + "/" + key;
        String signature = sign(stringToSign, c.getAccessKeySecret());

        URL url = new URL(c.getEndpoint() + "/" + c.getBucket() + "/" + key);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Date", date);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Content-MD5", md5);
            conn.setRequestProperty("Authorization", "OSS " + c.getAccessKeyId() + ":" + signature);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }
            int sc = conn.getResponseCode();
            if (sc != 200 && sc != 201) {
                String err = readBody(conn.getErrorStream());
                throw new IOException("OSS PutObject failed: HTTP " + sc + " body=" + err);
            }
            log.info("OssFileStorage.store: key={}, size={}", key, body.length);
            // Drain the success body to allow connection reuse
            drain(conn.getInputStream());
            return key;
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public void delete(String storageKey) {
        FileStorageProperties.Oss c = props.getOss();
        String date = GMT.format(java.time.Instant.now());
        String stringToSign = "DELETE\n\n\n" + date + "\n/oss-api//" + c.getBucket() + "/" + storageKey;
        String signature = sign(stringToSign, c.getAccessKeySecret());
        try {
            URL url = new URL(c.getEndpoint() + "/" + c.getBucket() + "/" + storageKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Date", date);
                conn.setRequestProperty("Authorization", "OSS " + c.getAccessKeyId() + ":" + signature);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                int sc = conn.getResponseCode();
                if (sc != 204 && sc != 200 && sc != 404) {
                    log.warn("OSS DeleteObject unexpected status {} body={}", sc, readBody(conn.getErrorStream()));
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            log.warn("OSS DeleteObject failed: key={}, err={}", storageKey, e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String storageKey) {
        FileStorageProperties.Oss c = props.getOss();
        if (!isBlank(c.getCdnDomain())) {
            String base = c.getCdnDomain();
            if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            return base + "/" + storageKey;
        }
        return trimTrailingSlash(c.getEndpoint()) + "/" + c.getBucket() + "/" + storageKey;
    }

    // --- helpers ---

    private static String sign(String stringToSign, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGO));
            byte[] sig = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new IllegalStateException("OSS HMAC failure", e);
        }
    }

    private static String md5Base64(byte[] data) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(data);
            return Base64.getEncoder().encodeToString(md5);
        } catch (Exception e) {
            throw new IllegalStateException("MD5 failure", e);
        }
    }

    private static String sanitizeName(String name) {
        if (name.contains("/") || name.contains("..") || name.contains("\\")) {
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
        return name;
    }

    private static String readBody(InputStream in) throws IOException {
        if (in == null) return "";
        try (InputStream stream = in) {
            byte[] buf = new byte[1024];
            int total = 0;
            int n;
            while ((n = stream.read(buf)) > 0) total += n;
            return total == 0 ? "" : new String(buf, 0, Math.min(total, buf.length), StandardCharsets.UTF_8);
        }
    }

    private static void drain(InputStream in) {
        if (in == null) return;
        try (InputStream stream = in) {
            byte[] buf = new byte[1024];
            while (stream.read(buf) > 0) { /* discard */ }
        } catch (IOException ignored) { }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String trimTrailingSlash(String s) { return s.endsWith("/") ? s.substring(0, s.length() - 1) : s; }
}