package com.smartteach.modules.course.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.course.dto.CourseContentSaveDTO;
import com.smartteach.modules.course.entity.CourseContent;
import com.smartteach.modules.course.service.CourseContentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 课程内容管理（章节下的课件/视频/文档等）
 */
@Api(tags = "课程计划管理-内容")
@RestController
@RequestMapping("/course/content")
@RequiredArgsConstructor
public class CourseContentController {

    private final CourseContentService contentService;

    @ApiOperation("按章节查询内容列表")
    @GetMapping("/list")
    public Result<List<CourseContent>> listByChapter(@RequestParam Long chapterId) {
        return Result.success(contentService.listByChapter(chapterId));
    }

    @ApiOperation("按课程查询全部内容")
    @GetMapping("/by-course")
    public Result<List<CourseContent>> listByCourse(@RequestParam Long courseId) {
        return Result.success(contentService.listByCourse(courseId));
    }

    @ApiOperation("新增内容")
    @PostMapping
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> add(@Valid @RequestBody CourseContentSaveDTO dto) {
        contentService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑内容")
    @PutMapping
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> edit(@Valid @RequestBody CourseContentSaveDTO dto) {
        contentService.update(dto);
        return Result.success();
    }

    @ApiOperation("批量删除内容")
    @DeleteMapping
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        contentService.remove(ids);
        return Result.success();
    }
}
