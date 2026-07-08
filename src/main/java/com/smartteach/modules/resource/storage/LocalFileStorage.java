package com.smartteach.modules.resource.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Default backend: writes the file under file.upload-path and exposes it through
 * file.access-prefix (which is mapped to that directory by WebMvcConfig).
 */
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final FileStorageProperties props;

    public LocalFileStorage(FileStorageProperties props) {
        this.props = props;
    }

    @Override
    public String store(String keyPrefix, MultipartFile file) throws IOException {
        if (props.getUploadPath() == null || props.getUploadPath().isEmpty()) {
            throw new IllegalStateException("file.upload-path is not configured");
        }
        // Normalize + ensure the resolved key stays under uploadPath (defense in depth)
        Path basePath = Paths.get(props.getUploadPath()).toAbsolutePath().normalize();
        Path target = basePath.resolve(keyPrefix).normalize();
        if (!target.startsWith(basePath)) {
            throw new IOException("Resolved path escapes upload root: " + target);
        }
        File dir = target.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
        Path dest = target.resolve(file.getOriginalFilename() != null ? file.getOriginalFilename()
                : java.util.UUID.randomUUID().toString());
        try (java.io.InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        // keyPrefix already contains the file name; we use it as the storage key so that
        // delete() can be driven from a known relative path.
        String key = keyPrefix + (keyPrefix.endsWith("/") ? "" : "/") + dest.getFileName().toString();
        log.info("LocalFileStorage.store: key={}, size={}", key, file.getSize());
        return key;
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isEmpty()) return;
        try {
            Path basePath = Paths.get(props.getUploadPath()).toAbsolutePath().normalize();
            Path target = basePath.resolve(storageKey).normalize();
            // Path.startsWith is case-sensitive on Windows even though NTFS is not.
            // Compare via lowercase absolute strings to avoid false negatives.
            String baseStr = basePath.toString().toLowerCase().replace("\\\\", "/");
            String targetStr = target.toString().toLowerCase().replace("\\\\", "/");
            if (!targetStr.startsWith(baseStr)) {
                log.warn("LocalFileStorage.delete: refusing (key escapes upload root). base={} target={}", basePath, target);
                return;
            }
            log.info("LocalFileStorage.delete: trying to delete target={}", target);
            boolean deleted = Files.deleteIfExists(target);
            log.info("LocalFileStorage.delete: key={} -> deleted={}", storageKey, deleted);
        } catch (IOException e) {
            log.warn("LocalFileStorage.delete failed: key={}, err={}", storageKey, e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String storageKey) {
        String prefix = props.getAccessPrefix() == null ? "/api/files/" : props.getAccessPrefix();
        if (!prefix.endsWith("/")) prefix = prefix + "/";
        return prefix + storageKey;
    }
}