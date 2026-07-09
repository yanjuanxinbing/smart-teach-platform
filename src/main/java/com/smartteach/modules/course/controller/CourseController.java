package com.smartteach.modules.course.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.dto.CourseQueryDTO;
import com.smartteach.modules.course.dto.CourseSaveDTO;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.service.CourseService;
import com.smartteach.modules.course.vo.CourseDetailVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 课程计划管理 - 课程信息
 */
@Api(tags = "课程计划管理-课程")
@RestController
@RequestMapping("/course/manage")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @ApiOperation("分页查询课程")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('course:list')")
    public Result<PageResult<Course>> page(CourseQueryDTO query) {
        return Result.success(courseService.page(query));
    }

    @ApiOperation("获取课程详情（基础信息+章节树+章节内容）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('course:query')")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    @ApiOperation("新增课程")
    @PostMapping
    @PreAuthorize("hasAuthority('course:add')")
    @OperationLog(module = "课程管理", action = "新增课程", saveParams = false)
    public Result<Void> add(@Valid @RequestBody CourseSaveDTO dto) {
        courseService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑课程")
    @PutMapping
    @PreAuthorize("hasAuthority('course:edit')")
    @OperationLog(module = "课程管理", action = "编辑课程", saveParams = false)
    public Result<Void> edit(@Valid @RequestBody CourseSaveDTO dto) {
        courseService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除课程")
    @DeleteMapping
    @PreAuthorize("hasAuthority('course:remove')")
    @OperationLog(module = "课程管理", action = "批量删除课程", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        courseService.remove(ids);
        return Result.success();
    }

    @ApiOperation("发布/停用课程")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('course:edit')")
    @OperationLog(module = "课程管理", action = "修改课程状态", saveParams = false)
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.changeStatus(id, status);
        return Result.success();
    }

    @ApiOperation("查询当前教师所授课程")
    @GetMapping("/my")
    public Result<List<Course>> myCourses() {
        return Result.success(courseService.listByTeacher(UserContext.getUserId()));
    }

    @ApiOperation("查询全部启用课程（下拉选择使用）")
    @GetMapping("/list-all")
    @PreAuthorize("hasAuthority('course:list')")
    public Result<List<Course>> listAll() {
        return Result.success(courseService.listAllEnabled());
    }
}
