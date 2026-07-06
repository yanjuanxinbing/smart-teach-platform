package com.smartteach.modules.resource.dto;

import lombok.Data;

@Data
public class ResourceSaveDTO {
    private Long id;
    private String resourceName;
    private Integer resourceType;
    private Long categoryId;
    private String categoryName;
    private Long courseId;
    private String courseName;
    private String originalName;
    private String filePath;
    private String fileUrl;
    private String fileSuffix;
    private Long fileSize;
    private String contentType;
    private String tags;
    private String description;
    private Integer status;
}
