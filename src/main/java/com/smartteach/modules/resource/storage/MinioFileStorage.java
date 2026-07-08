package com.smartteach.modules.resource.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * S3-compatible backend. Targets MinIO out of the box but also works against
 * AWS S3 / Ceph RGW / anything that speaks the S3 API. Uses AWS Signature V4.
 * Java 8 compatible (uses HttpURLConnection, no third-party SDK required).
 */
@Slf4j
public class MinioFileStorage implements FileStorage {

    private static final String ALGO = "HmacSHA256";
    private static final String SERVICE = "s3";
    private static final String REGION = "us-east-1";
    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final FileStorageProperties props;

    public MinioFileStorage(FileStorageProperties props) {
        this.props = props;
        validate();
    }

    private void validate() {
        FileStorageProperties.Minio c = props.getMinio();
        if (isBlank(c.getEndpoint()) || isBlank(c.getBucket())
                || isBlank(c.getAccessKey()) || isBlank(c.getSecretKey())) {
            throw new IllegalStateException(
                    "file.minio.{endpoint,bucket,access-key,secret-key} must be configured");
        }
    }

    @Override
    public String store(String keyPrefix, MultipartFile file) throws IOException {
        FileStorageProperties.Minio c = props.getMinio();
        String fileName = file.getOriginalFilename() == null
                ? java.util.UUID.randomUUID().toString()
                : sanitizeName(file.getOriginalFilename());
        String key = (keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/") + fileName;

        byte[] body = file.getBytes();
        String amzDate = AMZ_DATE.format(ZonedDateTime.now(ZoneOffset.UTC));
        String contentSha256 = sha256Hex(body);

        HttpURLConnection conn = (HttpURLConnection) buildUrl(c.getEndpoint(), c.getBucket(), key).openConnection();
        try {
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Host", hostHeader(c.getEndpoint()));
            conn.setRequestProperty("x-amz-content-sha256", contentSha256);
            conn.setRequestProperty("x-amz-date", amzDate);
            conn.setRequestProperty("Authorization", sign("PUT", c, key, amzDate, contentSha256));
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }
            int sc = conn.getResponseCode();
            if (sc != 200 && sc != 201) {
                String err = readBody(conn.getErrorStream());
                throw new IOException("S3 PutObject failed: HTTP " + sc + " body=" + err);
            }
            log.info("MinioFileStorage.store: key={}, size={}", key, body.length);
            drain(conn.getInputStream());
            return key;
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public void delete(String storageKey) {
        FileStorageProperties.Minio c = props.getMinio();
        String amzDate = AMZ_DATE.format(ZonedDateTime.now(ZoneOffset.UTC));
        try {
            HttpURLConnection conn = (HttpURLConnection) buildUrl(c.getEndpoint(), c.getBucket(), storageKey).openConnection();
            try {
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Host", hostHeader(c.getEndpoint()));
                conn.setRequestProperty("x-amz-content-sha256", EMPTY_SHA256);
                conn.setRequestProperty("x-amz-date", amzDate);
                conn.setRequestProperty("Authorization", sign("DELETE", c, storageKey, amzDate, EMPTY_SHA256));
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                int sc = conn.getResponseCode();
                if (sc != 204 && sc != 200 && sc != 404) {
                    log.warn("S3 DeleteObject unexpected status {} body={}", sc, readBody(conn.getErrorStream()));
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            log.warn("S3 DeleteObject failed: key={}, err={}", storageKey, e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String storageKey) {
        FileStorageProperties.Minio c = props.getMinio();
        String base = isBlank(c.getPublicBaseUrl()) ? c.getEndpoint() : c.getPublicBaseUrl();
        return trimTrailingSlash(base) + "/" + c.getBucket() + "/" + storageKey;
    }

    // --- V4 signing ---

    private String sign(String verb, FileStorageProperties.Minio c, String key,
                        String amzDate, String contentSha256) {
        try {
            String host = hostHeader(c.getEndpoint());
            String canonicalUri = "/" + c.getBucket() + "/" + key;
            String canonicalHeaders = "host:" + host + "\n"
                    + "x-amz-content-sha256:" + contentSha256 + "\n"
                    + "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";

            String canonicalRequest = verb + "\n" + canonicalUri + "\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + contentSha256;

            String shortDate = amzDate.substring(0, 8);
            String credentialScope = shortDate + "/" + REGION + "/" + SERVICE + "/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + credentialScope + "\n"
                    + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            byte[] kDate = hmac(("AWS4" + c.getSecretKey()).getBytes(StandardCharsets.UTF_8), shortDate);
            byte[] kRegion = hmac(kDate, REGION);
            byte[] kService = hmac(kRegion, SERVICE);
            byte[] kSigning = hmac(kService, "aws4_request");
            byte[] sig = hmac(kSigning, stringToSign);

            return "AWS4-HMAC-SHA256 Credential=" + c.getAccessKey() + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders
                    + ", Signature=" + toHex(sig);
        } catch (Exception e) {
            throw new IllegalStateException("S3 V4 signing failure", e);
        }
    }

    private static byte[] hmac(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance(ALGO);
        mac.init(new SecretKeySpec(key, ALGO));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(byte[] data) {
        try {
            return toHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String hostHeader(String endpoint) {
        URI u = URI.create(endpoint);
        int port = u.getPort();
        if (port < 0) return u.getHost();
        return u.getHost() + ":" + port;
    }


    /** Java 8 compatible hex encoder (HexFormat is Java 17+). */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
    private static URL buildUrl(String endpoint, String bucket, String key) {
        try { return URI.create(trimTrailingSlash(endpoint) + "/" + bucket + "/" + key).toURL(); } catch (java.net.MalformedURLException e) { throw new IllegalStateException("Invalid endpoint URL: " + endpoint, e); }
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