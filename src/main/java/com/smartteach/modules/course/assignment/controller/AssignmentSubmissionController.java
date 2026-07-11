package com.smartteach.modules.course.assignment.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.course.assignment.dto.AssignmentGradeDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionSaveDTO;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.service.AssignmentSubmissionService;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 作业提交管理
 *
 * <ul>
 *   <li>{@code /assignment/submission/page}：教师批改端，按作业 id / 学生姓名筛选</li>
 *   <li>{@code /assignment/submission/my-page}：学生本人提交列表（studentId 服务端注入）</li>
 *   <li>{@code /assignment/submission/latest?assignmentId=X}：学生对指定作业的最新记录</li>
 *   <li>{@code /save-draft} / {@code /submit}：学生写草稿 / 正式提交</li>
 *   <li>{@code /{id}/grade}：教师批改打分</li>
 * </ul>
 */
@Api(tags = "作业管理-提交")
@RestController
@RequestMapping("/assignment/submission")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;

    @ApiOperation("分页查询提交记录（教师）")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('assignment:submission:list')")
    public Result<PageResult<AssignmentSubmission>> page(AssignmentSubmissionQueryDTO query) {
        return Result.success(submissionService.page(query));
    }

    @ApiOperation("当前学生本人的提交列表")
    @GetMapping("/my-page")
    @PreAuthorize("hasAuthority('assignment:my:list')")
    public Result<PageResult<AssignmentSubmission>> myPage(AssignmentSubmissionQueryDTO query) {
        return Result.success(submissionService.myPage(query));
    }

    @ApiOperation("学生对指定作业的最新记录")
    @GetMapping("/latest")
    @PreAuthorize("hasAuthority('assignment:my:query')")
    public Result<AssignmentSubmission> latest(@RequestParam Long assignmentId) {
        return Result.success(submissionService.latest(assignmentId, null));
    }

    @ApiOperation("保存草稿")
    @PostMapping("/save-draft")
    @PreAuthorize("hasAuthority('assignment:save')")
    @OperationLog(module = "作业管理", action = "保存草稿", saveParams = false)
    public Result<Long> saveDraft(@Valid @RequestBody AssignmentSubmissionSaveDTO dto) {
        return Result.success(submissionService.saveDraft(dto));
    }

    @ApiOperation("学生正式提交")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('assignment:submit')")
    @OperationLog(module = "作业管理", action = "提交作业", saveParams = false)
    public Result<Void> submit(@Valid @RequestBody AssignmentSubmissionSaveDTO dto) {
        submissionService.submit(dto);
        return Result.success();
    }

    @ApiOperation("教师批改打分")
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('assignment:grade')")
    @OperationLog(module = "作业管理", action = "批改作业", saveParams = false)
    public Result<Void> grade(@PathVariable Long id, @Valid @RequestBody AssignmentGradeDTO dto) {
        submissionService.grade(id, dto.getScore(), dto.getComment());
        return Result.success();
    }

    @ApiOperation("删除提交记录（教师 / 学生均可）")
    @DeleteMapping
    @PreAuthorize("hasAuthority('assignment:remove') or hasAuthority('assignment:my:remove')")
    @OperationLog(module = "作业管理", action = "删除提交", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        submissionService.remove(ids);
        return Result.success();
    }
}
