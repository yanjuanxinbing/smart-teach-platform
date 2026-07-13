package com.smartteach.modules.portal.dto;

import lombok.Data;

/**
 * 前台门户 - 课程中心查询条件（公开，无需登录）
 */
@Data
public class PortalCourseQueryDTO {
    private Long current = 1L;
    private Long size = 12L;

    /** 课程性质标签：required 必修 / elective 选修 / lab 实验 / training 实训 */
    private String tag;
    /** 难度：easy / medium / hard */
    private String level;
    /** 关键字模糊匹配课程名/课程编号 */
    private String q;
}
