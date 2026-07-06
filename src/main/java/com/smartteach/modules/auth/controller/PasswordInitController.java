package com.smartteach.modules.auth.controller;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 一次性密码重置接口：仅在"密码初始化/找回管理员密码"场景下使用。
 *
 * 默认关闭：仅当 application.yaml 中 init.password-reset.enabled=true 时启用。
 * 生产环境请务必关闭！
 */
@Slf4j
@Api(tags = "密码初始化")
@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
public class PasswordInitController {

    @Value("${init.password-reset.enabled:false}")
    private boolean enabled;

    @Value("${init.password-reset.token:}")
    private String token;

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation("重置指定用户的密码（带token鉴权）")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        if (!enabled) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "密码重置接口未启用");
        }
        String tk = body.get("token");
        if (token == null || token.isEmpty() || !token.equals(tk)) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "token 无效");
        }
        String username = body.get("username");
        String newPassword = body.get("newPassword");
        if (username == null || newPassword == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("通过初始化接口重置用户 [{}] 的密码", username);
        return Result.success();
    }
}
