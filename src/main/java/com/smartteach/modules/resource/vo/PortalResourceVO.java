package com.smartteach.modules.resource.vo;

import com.smartteach.modules.resource.entity.Resource;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 门户侧资源 VO —— 精简字段,不暴露 file_path / create_by / update_by 等内部审计字段
 *
 * <p>对接前端契约见 web-portal/src/api/resource.js</p>
 */
@Data
public class PortalResourceVO {

    private Long id;

    private String resourceName;

    /** 资源类型 1文档 2图片 3视频 4音频 5压缩包 6其他 */
    private Integer resourceType;

    private Long categoryId;
    private String categoryName;

    private Long courseId;
    private String courseName;

    private String originalName;
    private String fileUrl;
    private String fileSuffix;
    private Long fileSize;
    private String contentType;

    private String tags;
    private String description;

    private Integer downloadCount;
    private Integer viewCount;

    /** 上传人 */
    private Long uploadBy;
    private String uploadName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static PortalResourceVO from(Resource r) {
        PortalResourceVO vo = new PortalResourceVO();
        BeanUtils.copyProperties(r, vo);
        return vo;
    }
}
