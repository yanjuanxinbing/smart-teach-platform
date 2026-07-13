package com.smartteach.modules.profile.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.profile.dto.ChangeOwnPasswordDTO;
import com.smartteach.modules.profile.dto.MyLogQueryDTO;
import com.smartteach.modules.profile.dto.UpdateMyProfileDTO;
import com.smartteach.modules.systemmonitor.entity.SysOperationLog;
import com.smartteach.modules.systemmonitor.service.SysOperationLogService;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.service.SysUserService;
import com.smartteach.modules.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 个人中心 & 我的日志
 * <p>
 * 三个前端（portal / admin 个人中心）共用：均要求登录。
 */
@Api(tags = "个人中心")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SysUserService userService;
    private final SysOperationLogService operationLogService;

    @ApiOperation("获取我的基本信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getDetail(userId));
    }

    @ApiOperation("更新我的资料")
    @PutMapping("/me")
    public Result<Void> updateMe(@Valid @RequestBody UpdateMyProfileDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser u = new SysUser();
        u.setId(userId);
        u.setRealName(dto.getRealName());
        u.setEmail(dto.getEmail());
        u.setPhone(dto.getPhone());
        u.setAvatar(dto.getAvatar());
        // bio 与 nickname 当前 sys_user 没有列，做兼容：如果有 edu_* 列就 PUT；
        // 本项目先用 MYSQL 自带列：realName / email / phone / avatar 即可。
        userService.updateById(u);
        return Result.success();
    }

    @ApiOperation("修改自己的密码")
    @PostMapping("/me/password")
    public Result<Void> changePwd(@Valid @RequestBody ChangeOwnPasswordDTO dto) {
        Long userId = UserContext.getUserId();
        userService.changeOwnPassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @ApiOperation("分页查询我的操作日志")
    @GetMapping("/me/logs")
    public Result<PageResult<SysOperationLog>> myLogs(MyLogQueryDTO query) {
        Long userId = UserContext.getUserId();
        // 复用系统监控的 service，但强制按 currentUserId 过滤（系统管理 / 我的日志 都用同一张表，差异在 userId）
        PageResult<SysOperationLog> page = operationLogService.pageOfUser(userId, query);
        return Result.success(page);
    }
}
