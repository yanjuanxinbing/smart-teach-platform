package com.smartteach.modules.resource.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration bound from application.yaml's "file" block.
 *
 * Example:
 *   file:
 *     storage-type: local | oss | minio
 *     upload-path: /var/smart-teach/uploads/        (local only)
 *     access-prefix: /api/files/                    (local only)
 *     oss:
 *       endpoint: https://oss-cn-hangzhou.aliyuncs.com
 *       bucket: my-bucket
 *       access-key-id: LTAI...
 *       access-key-secret: xxx
 *       cdn-domain: https://cdn.example.com         (optional, replaces endpoint in access URL)
 *     minio:
 *       endpoint: http://minio.local:9000
 *       bucket: my-bucket
 *       access-key: minioadmin
 *       secret-key: minioadmin
 *       public-base-url: http://minio.local:9000    (optional, override for CDN/proxy)
 */
@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    /** "local" | "oss" | "minio" */
    private String storageType = "local";

    /** Local backend: absolute path where files are written. */
    private String uploadPath;

    /** Local backend: URL prefix the static handler maps to uploadPath. */
    private String accessPrefix;

    private Oss oss = new Oss();
    private Minio minio = new Minio();

    @Data
    public static class Oss {
        private String endpoint;
        private String bucket;
        private String accessKeyId;
        private String accessKeySecret;
        /** Optional. If set, access URLs are built as {cdnDomain}/{key} instead of {endpoint}/{bucket}/{key}. */
        private String cdnDomain;
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;
        /** Optional. Override scheme+host used in access URLs (e.g. when behind a reverse proxy / CDN). */
        private String publicBaseUrl;
    }
}