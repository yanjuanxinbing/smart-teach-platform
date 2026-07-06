package com.smartteach.modules.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学资源
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_resource")
public class Resource extends BaseEntity {

    /** 资源名称 */
    private String resourceName;

    /** 资源类型 1文档 2图片 3视频 4音频 5压缩包 6其他 */
    private Integer resourceType;

    /** 资源分类ID */
    private Long categoryId;

    /** 分类名称（冗余） */
    private String categoryName;

    /** 关联课程ID（可空） */
    private Long courseId;

    /** 课程名称（冗余） */
    private String courseName;

    /** 文件原始名 */
    private String originalName;

    /** 文件存储路径（相对路径） */
    private String filePath;

    /** 访问URL */
    private String fileUrl;

    /** 文件后缀 */
    private String fileSuffix;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME类型 */
    private String contentType;

    /** 标签，逗号分隔 */
    private String tags;

    /** 描述 */
    private String description;

    /** 下载次数 */
    private Integer downloadCount;

    /** 浏览次数 */
    private Integer viewCount;

    /** 状态 0下架 1上架 */
    private Integer status;

    /** 上传人ID */
    private Long uploadBy;

    /** 上传人姓名 */
    private String uploadName;
}
