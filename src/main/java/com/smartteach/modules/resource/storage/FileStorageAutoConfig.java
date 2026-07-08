package com.smartteach.modules.resource.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Picks the right FileStorage implementation based on file.storage-type in application.yaml.
 *
 *   file.storage-type=local  -> LocalFileStorage
 *   file.storage-type=oss    -> OssFileStorage  (Aliyun OSS, V1 signature)
 *   file.storage-type=minio  -> MinioFileStorage (S3-compatible, V4 signature)
 *
 * Unknown values fall back to local, but emit a warning so the misconfiguration is loud.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfig {

    @Bean
    public FileStorage fileStorage(FileStorageProperties props) {
        String type = props.getStorageType() == null ? "local" : props.getStorageType().trim().toLowerCase();
        switch (type) {
            case "oss":
                log.info("FileStorage backend: Aliyun OSS (bucket={}, endpoint={})",
                        props.getOss().getBucket(), props.getOss().getEndpoint());
                return new OssFileStorage(props);
            case "minio":
                log.info("FileStorage backend: S3/MinIO (bucket={}, endpoint={})",
                        props.getMinio().getBucket(), props.getMinio().getEndpoint());
                return new MinioFileStorage(props);
            case "local":
            default:
                if (!"local".equals(type)) {
                    log.warn("Unknown file.storage-type='{}', falling back to local", type);
                }
                log.info("FileStorage backend: local disk (path={})", props.getUploadPath());
                return new LocalFileStorage(props);
        }
    }
}