package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程内容（章节下的具体教学资源：课件、视频、文档等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_content")
public class CourseContent extends BaseEntity {

    /** 所属课程ID */
    private Long courseId;

    /** 所属章节ID */
    private Long chapterId;

    /** 内容标题 */
    private String contentTitle;

    /** 类型 1课件PPT 2视频 3文档 4链接 5富文本 */
    private Integer contentType;

    /** 资源ID（关联 sys_resource） */
    private Long resourceId;

    /** 资源URL（冗余存储，便于直接播放/预览） */
    private String resourceUrl;

    /** 富文本内容（contentType=5时使用） */
    private String richText;

    /** 排序 */
    private Integer sort;

    /** 学时 */
    private Integer hours;

    /** 状态 0隐藏 1显示 */
    private Integer status;
}
