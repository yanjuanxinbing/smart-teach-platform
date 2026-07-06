package com.smartteach.modules.course.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.course.dto.CourseChapterSaveDTO;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.service.CourseChapterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 课程章节管理
 */
@Api(tags = "课程计划管理-章节")
@RestController
@RequestMapping("/course/chapter")
@RequiredArgsConstructor
public class CourseChapterController {

    private final CourseChapterService chapterService;

    @ApiOperation("按课程ID查询章节列表")
    @GetMapping("/list")
    public Result<List<CourseChapter>> list(@RequestParam Long courseId) {
        return Result.success(chapterService.listByCourse(courseId));
    }

    @ApiOperation("新增章节")
    @PostMapping
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> add(@Valid @RequestBody CourseChapterSaveDTO dto) {
        chapterService.save(dto);
        return Result.success();
    }

    @ApiOperation("编辑章节")
    @PutMapping
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> edit(@Valid @RequestBody CourseChapterSaveDTO dto) {
        chapterService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除章节")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('course:edit')")
    public Result<Void> remove(@PathVariable Long id) {
        chapterService.remove(id);
        return Result.success();
    }
}
