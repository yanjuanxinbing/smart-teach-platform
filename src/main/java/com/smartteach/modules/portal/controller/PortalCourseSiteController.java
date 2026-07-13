package com.smartteach.modules.portal.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.mapper.CourseChapterMapper;
import com.smartteach.modules.course.mapper.CourseMapper;
import com.smartteach.modules.portal.dto.PortalCourseQueryDTO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台门户 - 课程中心（公开访问）。
 * <p>
 * 仅展示已发布（status = 1）的课程；不再需要鉴权。
 * 与后台课程管理复用同一 mapper / entity。
 */
@Api(tags = "门户-课程中心")
@RestController
@RequestMapping("/portal/course")
@RequiredArgsConstructor
public class PortalCourseSiteController {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper chapterMapper;
    private final SysUserMapper sysUserMapper;

    @ApiOperation("课程分页（公开）")
    @GetMapping("/page")
    public Result<PageResult<Course>> page(PortalCourseQueryDTO query) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        // 仅展示已发布课程
        wrapper.eq(Course::getStatus, 1);
        // 关键字匹配编号 / 名称 / 教师
        if (StringUtils.isNotBlank(query.getQ())) {
            String kw = query.getQ().trim();
            wrapper.and(w -> w.like(Course::getCourseCode, kw)
                    .or().like(Course::getCourseName, kw)
                    .or().like(Course::getTeacherName, kw));
        }
        // tag 简单映射
        if ("required".equalsIgnoreCase(query.getTag())) {
            wrapper.eq(Course::getCourseType, 1);
        } else if ("elective".equalsIgnoreCase(query.getTag())) {
            wrapper.eq(Course::getCourseType, 2);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        IPage<Course> page = courseMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("课程详情（公开）")
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return Result.success(course);
    }

    @ApiOperation("课程章节（公开）")
    @GetMapping("/{id}/chapters")
    public Result<List<CourseChapter>> chapters(@PathVariable Long id) {
        List<CourseChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, id)
                .orderByAsc(CourseChapter::getSort));
        return Result.success(chapters);
    }

    @ApiOperation("教师列表（公开）")
    @GetMapping("/teachers")
    public Result<List<SysUser>> teachers() {
        // 前台门户简化为：展示前 20 名启用用户
        List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .last("LIMIT 20"));
        return Result.success(users);
    }
}
