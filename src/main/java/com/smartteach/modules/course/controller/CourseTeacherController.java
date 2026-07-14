package com.smartteach.modules.course.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.course.dto.CourseTeacherAssignDTO;
import com.smartteach.modules.course.dto.CourseTeacherQueryDTO;
import com.smartteach.modules.course.entity.CourseTeacher;
import com.smartteach.modules.course.service.CourseTeacherService;
import com.smartteach.modules.course.vo.CourseTeacherVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "课程管理-授课管理")
@RestController
@RequestMapping("/course/teacher")
@RequiredArgsConstructor
public class CourseTeacherController {

    private final CourseTeacherService courseTeacherService;

    @ApiOperation("分页查询授课关系")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('teach:list')")
    public Result<PageResult<CourseTeacherVO>> page(CourseTeacherQueryDTO query) {
        return Result.success(courseTeacherService.page(query));
    }

    @ApiOperation("全量替换一门课程的授课教师（先删后插）")
    @PostMapping
    @PreAuthorize("hasAuthority('teach:add')")
    @OperationLog(module = "授课管理", action = "分配授课教师", saveParams = false)
    public Result<Void> assign(@Valid @RequestBody CourseTeacherAssignDTO dto) {
        courseTeacherService.assign(dto);
        return Result.success();
    }

    @ApiOperation("启用/禁用单条授课关系")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('teach:edit')")
    @OperationLog(module = "授课管理", action = "修改授课状态", saveParams = false)
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseTeacherService.changeStatus(id, status);
        return Result.success();
    }

    @ApiOperation("批量软删授课关系")
    @DeleteMapping
    @PreAuthorize("hasAuthority('teach:remove')")
    @OperationLog(module = "授课管理", action = "删除授课关系", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        courseTeacherService.remove(ids);
        return Result.success();
    }

    /**
     * 任意登录用户可读——给"教学计划/作业列表"的下拉回显教师名用。
     */
    @ApiOperation("查询某课程的所有授课教师（任意登录用户可读）")
    @GetMapping("/teachers-by-course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<UserVO>> teachersByCourse(@PathVariable Long courseId) {
        return Result.success(courseTeacherService.listTeachersByCourseId(courseId));
    }

    /**
     * 任意登录用户可读——给"教师工作台 / 我的授课"用。
     */
    @ApiOperation("查询某教师的所有授课关系（任意登录用户可读）")
    @GetMapping("/courses-by-teacher/{teacherId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CourseTeacher>> coursesByTeacher(@PathVariable Long teacherId) {
        return Result.success(courseTeacherService.listByTeacherId(teacherId));
    }
}