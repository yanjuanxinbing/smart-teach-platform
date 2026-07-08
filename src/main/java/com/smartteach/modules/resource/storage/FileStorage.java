package com.smartteach.modules.resource.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction over the file backend used by the resource module.
 * Implementations: {@link LocalFileStorage} (default), {@link OssFileStorage} (Aliyun OSS),
 * {@link MinioFileStorage} (any S3-compatible service, e.g. MinIO / Ceph RGW / AWS S3).
 *
 * Lifecycle:
 *   - upload: client calls POST /biz/resource/upload -> FileUploadController calls
 *     {@link #store}, gets back a storage key, returns the public access URL to the
 *     browser, browser posts that URL into biz_resource.file_url.
 *   - preview/download: browser just opens file_url directly, no extra server round-trip.
 *   - delete: ResourceServiceImpl.remove calls {@link #delete} with the storage key.
 */
public interface FileStorage {

    /**
     * Store a file under the given key prefix (typically yyyy/MM/dd).
     * Returns the storage key (relative path or object key) that can later be
     * passed to {@link #delete} and {@link #getAccessUrl}.
     */
    String store(String keyPrefix, MultipartFile file) throws IOException;

    /**
     * Remove the object. Must be idempotent: no-op if the key does not exist.
     */
    void delete(String storageKey);

    /**
     * Build the public access URL for a stored object. The browser opens this URL
     * directly to preview/download the file, so the bucket must allow public reads
     * (or you front it with a CDN / proxy).
     */
    String getAccessUrl(String storageKey);
}