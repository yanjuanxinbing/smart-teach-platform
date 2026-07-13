package com.smartteach.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 班级成员分配（整批替换语义）
 */
@Data
public class SysClassMemberDTO {

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    /** 待保留的成员用户ID列表；不在列表中的成员会被软删除 */
    private List<Long> userIds;

    /** 可选的中文角色名（"教师"/"学生"），用于分配前只展示某角色的用户；不影响实际写入 */
    private String roleName;
}