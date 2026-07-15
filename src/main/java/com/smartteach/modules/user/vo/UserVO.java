package com.smartteach.modules.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Long deptId;
    private String deptName;
    /** 学生所在班级名称（取第一个班级），仅在 /auth/me 中填充 */
    private String className;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Long> roleIds;
    private List<String> roleNames;
    /** 用户拥有的权限标识（菜单/按钮 permission 字段），用于前端 hasAuthority() */
    private List<String> permissions;
}
