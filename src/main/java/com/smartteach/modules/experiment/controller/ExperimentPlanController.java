package com.smartteach.modules.experiment.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.experiment.dto.ExperimentPlanQueryDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanSaveDTO;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.service.ExperimentPlanService;
import com.smartteach.modules.experiment.vo.ExperimentPlanDetailVO;
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
 * 课程实验计划管理
 */
@Api(tags = "课程实验计划管理")
@RestController
@RequestMapping("/experiment/plan")
@RequiredArgsConstructor
public class ExperimentPlanController {

    private final ExperimentPlanService planService;

    @ApiOperation("分页查询实验计划")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('experiment:plan:list')")
    public Result<PageResult<ExperimentPlan>> page(ExperimentPlanQueryDTO query) {
        return Result.success(planService.page(query));
    }

    @ApiOperation("获取实验计划详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('experiment:plan:query')")
    public Result<ExperimentPlanDetailVO> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @ApiOperation("新增实验计划")
    @PostMapping
    @PreAuthorize("hasAuthority('experiment:plan:add')")
    @OperationLog(module = "实验计划", action = "新增实验计划")
    public Result<Long> add(@Valid @RequestBody ExperimentPlanSaveDTO dto) {
        return Result.success(planService.save(dto).getId());
    }

    @ApiOperation("编辑实验计划")
    @PutMapping
    @PreAuthorize("hasAuthority('experiment:plan:edit')")
    @OperationLog(module = "实验计划", action = "编辑实验计划")
    public Result<Void> edit(@Valid @RequestBody ExperimentPlanSaveDTO dto) {
        planService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除实验计划")
    @DeleteMapping
    @PreAuthorize("hasAuthority('experiment:plan:remove')")
    @OperationLog(module = "实验计划", action = "删除实验计划")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        planService.remove(ids);
        return Result.success();
    }

    @ApiOperation("提交审核")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('experiment:plan:edit')")
    @OperationLog(module = "实验计划", action = "提交审核")
    public Result<Void> submit(@PathVariable Long id) {
        planService.submit(id);
        return Result.success();
    }

    @ApiOperation("审核通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('experiment:plan:approve')")
    @OperationLog(module = "实验计划", action = "审核通过")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.approve(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }

    @ApiOperation("审核驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('experiment:plan:approve')")
    @OperationLog(module = "实验计划", action = "审核驳回")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.reject(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }
}
