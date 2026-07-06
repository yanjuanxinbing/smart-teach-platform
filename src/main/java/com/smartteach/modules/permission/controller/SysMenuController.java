package com.smartteach.modules.permission.controller;

import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.permission.entity.SysMenu;
import com.smartteach.modules.permission.service.SysMenuService;
import com.smartteach.modules.permission.vo.MenuTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 权限管理 - 菜单/按钮维护
 */
@Api(tags = "权限管理-菜单")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @ApiOperation("获取全部菜单树（维护页使用）")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.tree());
    }

    @ApiOperation("获取当前登录用户的可访问菜单树（左侧导航渲染）")
    @GetMapping("/my-menu")
    public Result<List<MenuTreeVO>> myMenu() {
        return Result.success(menuService.treeByUserId(UserContext.getUserId()));
    }

    @ApiOperation("新增菜单/按钮")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@Valid @RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.success();
    }

    @ApiOperation("编辑菜单/按钮")
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> edit(@Valid @RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return Result.success();
    }

    @ApiOperation("删除菜单/按钮")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.removeMenu(id);
        return Result.success();
    }
}
