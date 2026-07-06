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
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Long> roleIds;
    private List<String> roleNames;
}
