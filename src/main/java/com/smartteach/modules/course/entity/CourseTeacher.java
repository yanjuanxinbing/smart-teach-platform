package com.smartteach.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程-教师授课关系（多对多）。
 * 一门课可以有多个教师（主讲 / 助教），一个教师可以授多门课。
 * 通过 BaseEntity 继承 @TableLogic 软删；deleted 与 status 各自独立：
 *   - deleted=1 表示行被移除（管理员看不到，但审计可查）
 *   - status=0 表示分配暂停（教师端 /myCourses 不返该课程，但行还在）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_teacher")
public class CourseTeacher extends BaseEntity {

    /** 课程ID */
    private Long courseId;

    /** 教师用户ID */
    private Long teacherId;

    /** 主讲 / 助教 */
    private String role;

    private Integer sort;

    /** 0禁用 1启用 */
    private Integer status;
}