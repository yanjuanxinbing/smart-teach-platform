package com.smartteach.modules.aiassignment.controller;

import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.aiassignment.dto.AnalyticsGenerateRequestDTO;
import com.smartteach.modules.aiassignment.service.AssignmentAnalyticsService;
import com.smartteach.modules.aiassignment.vo.AiAnalyticsReportVO;
import com.smartteach.modules.aiassignment.vo.ClassAssignmentStatsVO;
import com.smartteach.modules.aiassignment.vo.StudentAssignmentStatsVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 作业 AI 智能分析 Controller（教师班级视角 + 学生个人视角）
 *
 * <p>路径前缀 <code>/assignment/analytics</code>，对应 sys_menu 754 / 784。
 */
@Api(tags = "作业管理-AI 智能分析")
@RestController
@RequestMapping("/assignment/analytics")
@RequiredArgsConstructor
public class AssignmentAnalyticsController {

    private final AssignmentAnalyticsService svc;

    @ApiOperation("班级作业分析（教师）— 班级维度统计")
    @GetMapping("/class")
    @PreAuthorize("hasAuthority('assignment:analytics:class')")
    public Result<ClassAssignmentStatsVO> classStats(
            @RequestParam Long classId,
            @RequestParam(required = false) String semester) {
        return Result.success(svc.classStats(classId, semester));
    }

    @ApiOperation("学生本人作业分析（学生）— 取 UserContext")
    @GetMapping("/student/me")
    @PreAuthorize("hasAuthority('assignment:analytics:student')")
    public Result<StudentAssignmentStatsVO> myStats() {
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(svc.studentStats(studentId, null));
    }

    @ApiOperation("班级 AI 分析报告（教师）— 同步阻塞 5-30s")
    @PostMapping("/class/generate")
    @PreAuthorize("hasAuthority('assignment:analytics:class')")
    @OperationLog(module = "作业 AI 分析", action = "教师生成班级报告", saveParams = false)
    public Result<AiAnalyticsReportVO> generateClassReport(@Valid @RequestBody AnalyticsGenerateRequestDTO dto) {
        return Result.success(svc.generateClassReport(dto));
    }

    @ApiOperation("学生 AI 反思报告（学生）— 同步阻塞 5-30s")
    @PostMapping("/student/generate")
    @PreAuthorize("hasAuthority('assignment:analytics:student')")
    @OperationLog(module = "作业 AI 分析", action = "学生生成反思报告", saveParams = false)
    public Result<AiAnalyticsReportVO> generateStudentReport(@Valid @RequestBody AnalyticsGenerateRequestDTO dto) {
        return Result.success(svc.generateStudentReport(dto));
    }
}
