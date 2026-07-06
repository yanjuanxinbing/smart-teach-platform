package com.smartteach.modules.permission.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.permission.dto.RoleSaveDTO;
import com.smartteach.modules.permission.entity.SysRole;
import com.smartteach.modules.permission.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 权限管理 - 角色维护
 */
@Api(tags = "权限管理-角色")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @ApiOperation("分页查询角色列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<PageResult<SysRole>> page(PageQuery query) {
        return Result.success(roleService.page(query));
    }

    @ApiOperation("查询全部启用角色（下拉框使用）")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.listAllEnabled());
    }

    @ApiOperation("获取角色已分配的菜单ID")
    @GetMapping("/{id}/menu-ids")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.success(roleService.getMenuIdsByRoleId(id));
    }

    @ApiOperation("新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Void> add(@Valid @RequestBody RoleSaveDTO dto) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        roleService.saveRole(role, dto.getMenuIds());
        return Result.success();
    }

    @ApiOperation("编辑角色")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> edit(@Valid @RequestBody RoleSaveDTO dto) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        roleService.updateRole(role, dto.getMenuIds());
        return Result.success();
    }

    @ApiOperation("批量删除角色")
    @DeleteMapping
    @PreAuthorize("hasAuthority('system:role:remove')")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        roleService.removeByIds(ids);
        return Result.success();
    }
}
