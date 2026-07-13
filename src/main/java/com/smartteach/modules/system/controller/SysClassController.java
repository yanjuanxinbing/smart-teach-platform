package com.smartteach.modules.system.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.system.dto.SysClassMemberDTO;
import com.smartteach.modules.system.dto.SysClassQueryDTO;
import com.smartteach.modules.system.dto.SysClassSaveDTO;
import com.smartteach.modules.system.service.SysClassService;
import com.smartteach.modules.system.vo.SysClassVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 班级管理
 *
 * <ul>
 *   <li>CRUD：/page、/、/{id} — 角色 ADMIN / SYSTEM 可见</li>
 *   <li>下拉：/list-by-dept?deptId=X、/list-all — 任何登录用户都可调（教师作业表单、学生端）</li>
 *   <li>成员：/my-classes、/{id}/members、/members</li>
 * </ul>
 */
@Api(tags = "系统设置-班级")
@RestController
@RequestMapping("/system/class")
@RequiredArgsConstructor
public class SysClassController {

    private final SysClassService classService;

    @ApiOperation("分页查询班级")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('class:list')")
    public Result<PageResult<SysClassVO>> page(SysClassQueryDTO query) {
        return Result.success(classService.page(query));
    }

    @ApiOperation("班级详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('class:query')")
    public Result<SysClassVO> detail(@PathVariable Long id) {
        return Result.success(classService.detail(id));
    }

    @ApiOperation("按部门拉班级（用于院系级联下拉）")
    @GetMapping("/list-by-dept")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysClassVO>> listByDept(@RequestParam Long deptId) {
        return Result.success(classService.listByDept(deptId));
    }

    @ApiOperation("全量有效班级（用于下拉、列表渲染）")
    @GetMapping("/list-all")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysClassVO>> listAll() {
        return Result.success(classService.listAllEnabled());
    }

    @ApiOperation("当前登录用户所在的班级")
    @GetMapping("/my-classes")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysClassVO>> myClasses() {
        return Result.success(classService.listByCurrentUser());
    }

    @ApiOperation("班级成员列表（按中文角色名过滤：教师/学生；空=全部）")
    @GetMapping("/{id}/members")
    @PreAuthorize("hasAuthority('class:member:assign')")
    public Result<List<UserVO>> members(@PathVariable Long id,
                                       @RequestParam(required = false) String roleName) {
        return Result.success(classService.listMembers(id, roleName));
    }

    @ApiOperation("新增班级")
    @PostMapping
    @PreAuthorize("hasAuthority('class:add')")
    @OperationLog(module = "班级管理", action = "新增班级", saveParams = false)
    public Result<Void> add(@Valid @RequestBody SysClassSaveDTO dto) {
        classService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑班级")
    @PutMapping
    @PreAuthorize("hasAuthority('class:edit')")
    @OperationLog(module = "班级管理", action = "编辑班级", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody SysClassSaveDTO dto) {
        classService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除班级")
    @DeleteMapping
    @PreAuthorize("hasAuthority('class:remove')")
    @OperationLog(module = "班级管理", action = "删除班级", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        classService.remove(ids);
        return Result.success();
    }

    @ApiOperation("分配班级成员（整批替换）")
    @PostMapping("/members")
    @PreAuthorize("hasAuthority('class:member:assign')")
    @OperationLog(module = "班级管理", action = "分配班级成员", saveParams = false)
    public Result<Void> assignMembers(@Valid @RequestBody SysClassMemberDTO dto) {
        classService.assignMembers(dto);
        return Result.success();
    }
}