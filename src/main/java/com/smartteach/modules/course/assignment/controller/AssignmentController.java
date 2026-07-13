package com.smartteach.modules.course.assignment.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.course.assignment.dto.AssignmentQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSaveDTO;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.service.AssignmentService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 作业管理（教师侧：发布 / 编辑 / 删除 / 关闭）
 */
@Api(tags = "作业管理-作业")
@RestController
@RequestMapping("/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @ApiOperation("分页查询作业（教师端，按 query 过滤）")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('assignment:list') or hasAuthority('assignment:my:list')")
    public Result<PageResult<Assignment>> page(AssignmentQueryDTO query) {
        return Result.success(assignmentService.page(query));
    }

    @ApiOperation("学生端我的作业（按所在班级自动过滤）")
    @GetMapping("/my-page")
    @PreAuthorize("hasAuthority('assignment:my:list')")
    public Result<PageResult<Assignment>> myPage(AssignmentQueryDTO query) {
        return Result.success(assignmentService.myPage(query));
    }

    @ApiOperation("作业详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('assignment:query') or hasAuthority('assignment:my:query')")
    public Result<Assignment> detail(@PathVariable Long id) {
        return Result.success(assignmentService.detail(id));
    }

    @ApiOperation("新增作业（默认草稿）")
    @PostMapping
    @PreAuthorize("hasAuthority('assignment:add')")
    @OperationLog(module = "作业管理", action = "新增作业", saveParams = false)
    public Result<Void> add(@Valid @RequestBody AssignmentSaveDTO dto) {
        assignmentService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑作业")
    @PutMapping
    @PreAuthorize("hasAuthority('assignment:edit')")
    @OperationLog(module = "作业管理", action = "编辑作业", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody AssignmentSaveDTO dto) {
        assignmentService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除作业")
    @DeleteMapping
    @PreAuthorize("hasAuthority('assignment:remove')")
    @OperationLog(module = "作业管理", action = "删除作业", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        assignmentService.remove(ids);
        return Result.success();
    }

    @ApiOperation("发布作业（草稿→已发布）")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('assignment:publish')")
    @OperationLog(module = "作业管理", action = "发布作业", saveParams = false)
    public Result<Void> publish(@PathVariable Long id) {
        assignmentService.publish(id);
        return Result.success();
    }

    @ApiOperation("关闭作业（已发布→已截止）")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAuthority('assignment:close')")
    @OperationLog(module = "作业管理", action = "关闭作业", saveParams = false)
    public Result<Void> close(@PathVariable Long id) {
        assignmentService.close(id);
        return Result.success();
    }
}
