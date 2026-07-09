package com.smartteach.modules.training.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.training.dto.TrainingPlanQueryDTO;
import com.smartteach.modules.training.dto.TrainingPlanSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.service.TrainingPlanService;
import com.smartteach.modules.training.vo.TrainingPlanDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 实训计划管理
 */
@Api(tags = "实训计划管理")
@RestController
@RequestMapping("/training/plan")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService planService;

    @ApiOperation("分页查询实训计划")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('training:plan:list')")
    public Result<PageResult<TrainingPlan>> page(TrainingPlanQueryDTO query) {
        return Result.success(planService.page(query));
    }

    @ApiOperation("获取实训计划详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('training:plan:query')")
    public Result<TrainingPlanDetailVO> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @ApiOperation("新增实训计划")
    @PostMapping
    @PreAuthorize("hasAuthority('training:plan:add')")
    @OperationLog(module = "实训计划", action = "新增实训计划", saveParams = false)
    public Result<Void> add(@Valid @RequestBody TrainingPlanSaveDTO dto) {
        planService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑实训计划")
    @PutMapping
    @PreAuthorize("hasAuthority('training:plan:edit')")
    @OperationLog(module = "实训计划", action = "编辑实训计划", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody TrainingPlanSaveDTO dto) {
        planService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除实训计划")
    @DeleteMapping
    @PreAuthorize("hasAuthority('training:plan:remove')")
    @OperationLog(module = "实训计划", action = "删除实训计划", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        planService.remove(ids);
        return Result.success();
    }

    @ApiOperation("发布")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    @OperationLog(module = "实训计划", action = "发布实训计划", saveParams = false)
    public Result<Void> publish(@PathVariable Long id) {
        planService.publish(id);
        return Result.success();
    }

    @ApiOperation("提交审核")
    @PutMapping("/{id}/submit-review")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    @OperationLog(module = "实训计划", action = "提交审核")
    public Result<Void> submitReview(@PathVariable Long id) {
        planService.submitForReview(id);
        return Result.success();
    }

    @ApiOperation("退回草稿")
    @PutMapping("/{id}/revert-draft")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    @OperationLog(module = "实训计划", action = "退回草稿")
    public Result<Void> revertDraft(@PathVariable Long id) {
        planService.revertToDraft(id);
        return Result.success();
    }

    @ApiOperation("完结")
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    @OperationLog(module = "实训计划", action = "完结实训计划", saveParams = false)
    public Result<Void> finish(@PathVariable Long id) {
        planService.finish(id);
        return Result.success();
    }

    @ApiOperation("审核通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('training:plan:approve')")
    @OperationLog(module = "实训计划", action = "审核通过", saveParams = false)
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.approve(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }

    @ApiOperation("审核驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('training:plan:approve')")
    @OperationLog(module = "实训计划", action = "审核驳回", saveParams = false)
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.reject(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }
}
