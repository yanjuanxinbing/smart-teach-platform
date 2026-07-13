package com.smartteach.modules.auth.controller;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.auth.dto.LoginDTO;
import com.smartteach.modules.auth.dto.RegisterDTO;
import com.smartteach.modules.auth.service.AuthService;
import com.smartteach.modules.auth.vo.LoginVO;
import com.smartteach.modules.systemmonitor.service.SysLoginLogService;
import com.smartteach.modules.user.mapper.SysUserMapper;
import com.smartteach.modules.user.service.SysUserService;
import com.smartteach.modules.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证模块：登录、登出、获取当前登录用户信息
 */
@Api(tags = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysLoginLogService loginLogService;

    @ApiOperation("账号密码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        try {
            LoginVO vo = authService.login(dto);
            loginLogService.record(dto.getUsername(), ip, ua, 1, "登录成功");
            return Result.success(vo);
        } catch (BusinessException e) {
            loginLogService.record(dto.getUsername(), ip, ua, 0, e.getMessage());
            throw e;
        }
    }

    @ApiOperation("自助注册（教师/学生）")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        try {
            LoginVO vo = authService.register(dto);
            loginLogService.record(dto.getUsername(), ip, ua, 1, "注册并登录");
            return Result.success(vo);
        } catch (BusinessException e) {
            loginLogService.record(dto.getUsername(), ip, ua, 0, e.getMessage());
            throw e;
        }
    }

    @ApiOperation("退出登录（前端清除本地token即可，此接口预留用于记录日志/token黑名单等扩展）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = UserContext.getUserId();
        UserVO vo = userService.getDetail(userId);
        vo.setRoleNames(userMapper.selectRoleCodesByUserId(userId));
        // 同时返回权限标识，让前端在页面刷新后能恢复 hasAuthority() 的判断依据
        vo.setPermissions(userMapper.selectPermissionsByUserId(userId));
        return Result.success(vo);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}