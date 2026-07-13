package com.smartteach.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作业-班级 关系（M:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment_target_class")
public class SysAssignmentClass extends BaseEntity {

    private Long assignmentId;
    private Long classId;
}