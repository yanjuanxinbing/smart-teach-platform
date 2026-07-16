package com.smartteach.modules.portal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.portal.service.PortalMyLearningService;
import com.smartteach.modules.portal.vo.PortalExperimentDetailVO;
import com.smartteach.modules.portal.vo.PortalMyAssignmentVO;
import com.smartteach.modules.portal.vo.PortalMyCourseVO;
import com.smartteach.modules.portal.vo.PortalMyExperimentVO;
import com.smartteach.modules.portal.vo.PortalMyTrainingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * 「我的学习中心」门户侧接口 —— 学生专属
 *
 * <p>反 IDOR：studentId 一律从 UserContext.getUserId() 取，**绝不**接受查询参数</p>
 * <p>对接前端契约见 web-portal/src/api/my.js</p>
 */
@Api(tags = "门户-我的学习中心")
@RestController
@RequestMapping("/portal/my")
@RequiredArgsConstructor
public class PortalMyLearningController {

    private final PortalMyLearningService myLearningService;

    @ApiOperation("我的课程（已选课程分页）")
    @GetMapping("/courses")
    @PreAuthorize("hasAuthority('course:my:list')")
    public Result<PageResult<PortalMyCourseVO>> myCourses(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String q) {
        Long studentId = requireStudent();
        IPage<PortalMyCourseVO> page = myLearningService.myCourses(studentId, current, size, q);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("加入课程（幂等：已选直接返回现有记录）")
    @PostMapping("/courses/add")
    @PreAuthorize("hasAuthority('course:my:list')")
    public Result<PortalMyCourseVO> joinCourse(@RequestBody JoinCourseRequest body) {
        Long studentId = requireStudent();
        if (body == null || body.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        PortalMyCourseVO vo = myLearningService.joinCourse(studentId, body.getCourseId());
        return Result.success(vo);
    }

    @ApiOperation("我的作业（按状态过滤：pending/submitted/graded）")
    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('assignment:my:list')")
    public Result<PageResult<PortalMyAssignmentVO>> myAssignments(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String status) {
        Long studentId = requireStudent();
        IPage<PortalMyAssignmentVO> page = myLearningService.myAssignments(studentId, current, size, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("我的实训（按状态过滤：pending_review/not_started/in_progress/done）")
    @GetMapping("/trainings")
    @PreAuthorize("hasAuthority('training:my:list')")
    public Result<PageResult<PortalMyTrainingVO>> myTrainings(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String status) {
        Long studentId = requireStudent();
        IPage<PortalMyTrainingVO> page = myLearningService.myTrainings(studentId, current, size, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("我的实验（仅返回当前学生被分配的实验，item 状态由 classDate 推算）")
    @GetMapping("/experiments")
    @PreAuthorize("hasAuthority('experiment:my:list')")
    public Result<PageResult<PortalMyExperimentVO>> myExperiments(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        Long studentId = requireStudent();
        IPage<PortalMyExperimentVO> page = myLearningService.myExperiments(studentId, current, size, status, q);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("实验详情（item 级别，含目的/内容/要求 + 学生当前分配状态）")
    @GetMapping("/experiments/{id}")
    @PreAuthorize("hasAuthority('experiment:my:list')")
    public Result<PortalExperimentDetailVO> experimentDetail(@PathVariable("id") Long id) {
        Long studentId = requireStudent();
        return Result.success(myLearningService.getExperimentDetail(studentId, id));
    }

    // --- 工具 ---

    /**
     * 1) 校验当前 thread 是否携带已认证的用户;否则视为未登录。
     * 2) 以 JWT 过滤器注入的 UserContext.userId 为唯一 studentId 来源,**拒绝任何 query/body 中的同名字段**。
     */
    private Long requireStudent() {
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return studentId;
    }

    /**
     * 加入课程请求体 —— 仅含 courseId(从 body 读,不从 URL 读,避免日志外泄);
     * studentId 严格从 JWT 拿,绝不接受请求参数里的 studentId(反 IDOR)。
     */
    @lombok.Data
    public static class JoinCourseRequest {
        @NotNull
        private Long courseId;
    }
}
