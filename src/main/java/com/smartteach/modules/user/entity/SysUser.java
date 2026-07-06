package com.smartteach.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** 密码（BCrypt加密存储） */
    private String password;

    /** 姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 所属部门ID */
    private Long deptId;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
