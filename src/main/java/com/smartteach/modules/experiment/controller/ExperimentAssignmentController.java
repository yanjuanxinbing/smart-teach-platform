package com.smartteach.modules.experiment.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.experiment.entity.ExperimentAssignment;
import com.smartteach.modules.experiment.service.ExperimentAssignmentService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 实验分配管理 —— 教师/管理员给班级或单个学生分配实验计划
 *
 * <p>与 {@link TrainingRegistrationController} 对称；
 * 本控制器是"分配"语义（教师主导），没有审核端点（学生不发起报名）。</p>
 */
@Api(tags = "实验计划管理-分配")
@RestController
@RequestMapping("/experiment/assignment")
@RequiredArgsConstructor
public class ExperimentAssignmentController {

    private final ExperimentAssignmentService assignmentService;

    @ApiOperation("分页查询分配记录")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('experiment:assignment:list')")
    public Result<PageResult<ExperimentAssignment>> page(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String className,
                                                         @RequestParam(required = false) Integer status,
                                                         PageQuery query) {
        return Result.success(assignmentService.page(keyword, className, status, query));
    }

    @ApiOperation("按班级分配：自动给该班每个学生铺一条 status=1 的记录，返回新增条数")
    @PostMapping
    @PreAuthorize("hasAuthority('experiment:assignment:add')")
    @OperationLog(module = "实验分配", action = "按班级分配", saveParams = false)
    public Result<Integer> assignByClass(@Valid @RequestBody AssignByClassRequest body) {
        Long operatorId = UserContext.getUserId();
        int added = assignmentService.assignByClass(body.getPlanId(), body.getClassName(), operatorId);
        return Result.success(added);
    }

    @ApiOperation("单个学生手动分配")
    @PostMapping("/one")
    @PreAuthorize("hasAuthority('experiment:assignment:add')")
    @OperationLog(module = "实验分配", action = "单个分配", saveParams = false)
    public Result<ExperimentAssignment> assignOne(@Valid @RequestBody AssignOneRequest body) {
        Long operatorId = UserContext.getUserId();
        return Result.success(assignmentService.assignOne(body.getPlanId(), body.getStudentId(), operatorId));
    }

    @ApiOperation("标记完成：录成绩 + 状态置 3")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('experiment:assignment:complete')")
    @OperationLog(module = "实验分配", action = "标记完成", saveParams = false)
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object scoreObj = body.get("score");
        Object commentObj = body.get("comment");
        BigDecimal score = scoreObj == null ? null : new BigDecimal(scoreObj.toString());
        String comment = commentObj == null ? null : commentObj.toString();
        assignmentService.markCompleted(id, score, comment);
        return Result.success();
    }

    @ApiOperation("批量撤销分配（软删）")
    @DeleteMapping
    @PreAuthorize("hasAuthority('experiment:assignment:remove')")
    @OperationLog(module = "实验分配", action = "撤销分配", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        assignmentService.remove(ids);
        return Result.success();
    }

    // --- 请求体 ---

    @lombok.Data
    public static class AssignByClassRequest {
        @NotNull
        private Long planId;
        @NotEmpty
        private String className;
    }

    @lombok.Data
    public static class AssignOneRequest {
        @NotNull
        private Long planId;
        @NotNull
        private Long studentId;
    }
}