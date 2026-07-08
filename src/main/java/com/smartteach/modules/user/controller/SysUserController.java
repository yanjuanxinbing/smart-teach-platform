package com.smartteach.modules.user.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.user.dto.UserQueryDTO;
import com.smartteach.modules.user.dto.UserSaveDTO;
import com.smartteach.modules.user.service.SysUserService;
import com.smartteach.modules.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户管理模块
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @ApiOperation("分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVO>> page(UserQueryDTO query) {
        return Result.success(userService.page(query));
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.getDetail(id));
    }

    @ApiOperation("新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @OperationLog(module = "用户管理", action = "新增用户", saveParams = false)
    public Result<Void> add(@Valid @RequestBody UserSaveDTO dto) {
        userService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑用户")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    @OperationLog(module = "用户管理", action = "编辑用户", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody UserSaveDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除用户")
    @DeleteMapping
    @PreAuthorize("hasAuthority('system:user:remove')")
    @OperationLog(module = "用户管理", action = "批量删除用户", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }

    @ApiOperation("重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @OperationLog(module = "用户管理", action = "重置用户密码", saveParams = false)
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.success();
    }

    @ApiOperation("启用/禁用用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @OperationLog(module = "用户管理", action = "修改用户状态", saveParams = false)
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("当前登录用户修改自己的密码")
    @PutMapping("/change-password")
    @OperationLog(module = "用户管理", action = "修改自己的密码", saveParams = false)
    public Result<Void> changeOwnPassword(@RequestBody Map<String, String> body) {
        userService.changeOwnPassword(UserContext.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
}
