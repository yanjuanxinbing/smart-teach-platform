package com.smartteach.modules.course.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.dto.CoursePlanQueryDTO;
import com.smartteach.modules.course.dto.CoursePlanSaveDTO;
import com.smartteach.modules.course.entity.CoursePlan;
import com.smartteach.modules.course.service.CoursePlanService;
import com.smartteach.modules.course.vo.CoursePlanDetailVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 课程计划管理 - 教学进度计划
 */
@Api(tags = "课程计划管理-计划")
@RestController
@RequestMapping("/course/plan")
@RequiredArgsConstructor
public class CoursePlanController {

    private final CoursePlanService planService;

    @ApiOperation("分页查询计划列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('course:plan:list')")
    public Result<PageResult<CoursePlan>> page(CoursePlanQueryDTO query) {
        return Result.success(planService.page(query));
    }

    @ApiOperation("获取计划详情（含周次明细）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('course:plan:query')")
    public Result<CoursePlanDetailVO> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @ApiOperation("新增计划（同时保存周次明细）")
    @PostMapping
    @PreAuthorize("hasAuthority('course:plan:add')")
    @OperationLog(module = "教学计划", action = "新增计划", saveParams = false)
    public Result<Void> add(@Valid @RequestBody CoursePlanSaveDTO dto) {
        planService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑计划")
    @PutMapping
    @PreAuthorize("hasAuthority('course:plan:edit')")
    @OperationLog(module = "教学计划", action = "编辑计划", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody CoursePlanSaveDTO dto) {
        planService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除计划")
    @DeleteMapping
    @PreAuthorize("hasAuthority('course:plan:remove')")
    @OperationLog(module = "教学计划", action = "批量删除计划", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        planService.remove(ids);
        return Result.success();
    }

    @ApiOperation("提交审核")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('course:plan:edit')")
    @OperationLog(module = "教学计划", action = "提交计划审核", saveParams = false)
    public Result<Void> submit(@PathVariable Long id) {
        planService.submit(id);
        return Result.success();
    }

    @ApiOperation("审核通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('course:plan:approve')")
    @OperationLog(module = "教学计划", action = "审核通过计划", saveParams = false)
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.approve(id, UserContext.getUserId(),
                UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }

    @ApiOperation("审核驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('course:plan:approve')")
    @OperationLog(module = "教学计划", action = "审核驳回计划", saveParams = false)
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.reject(id, UserContext.getUserId(),
                UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }
}
