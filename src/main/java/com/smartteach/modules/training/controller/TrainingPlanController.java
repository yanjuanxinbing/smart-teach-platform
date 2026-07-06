package com.smartteach.modules.training.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.training.dto.TrainingPlanQueryDTO;
import com.smartteach.modules.training.dto.TrainingPlanSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.service.TrainingPlanService;
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
    public Result<TrainingPlan> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @ApiOperation("新增实训计划")
    @PostMapping
    @PreAuthorize("hasAuthority('training:plan:add')")
    public Result<Void> add(@Valid @RequestBody TrainingPlanSaveDTO dto) {
        planService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑实训计划")
    @PutMapping
    @PreAuthorize("hasAuthority('training:plan:edit')")
    public Result<Void> edit(@Valid @RequestBody TrainingPlanSaveDTO dto) {
        planService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除实训计划")
    @DeleteMapping
    @PreAuthorize("hasAuthority('training:plan:remove')")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        planService.remove(ids);
        return Result.success();
    }

    @ApiOperation("发布")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    public Result<Void> publish(@PathVariable Long id) {
        planService.publish(id);
        return Result.success();
    }

    @ApiOperation("完结")
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAuthority('training:plan:edit')")
    public Result<Void> finish(@PathVariable Long id) {
        planService.finish(id);
        return Result.success();
    }

    @ApiOperation("审核通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('training:plan:approve')")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.approve(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }

    @ApiOperation("审核驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('training:plan:approve')")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        planService.reject(id, UserContext.getUserId(), UserContext.getUsername(), body.get("remark"));
        return Result.success();
    }
}
