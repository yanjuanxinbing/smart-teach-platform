package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.dto.CourseQueryDTO;
import com.smartteach.modules.course.dto.CourseSaveDTO;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.entity.CourseContent;
import com.smartteach.modules.course.mapper.CourseChapterMapper;
import com.smartteach.modules.course.mapper.CourseContentMapper;
import com.smartteach.modules.course.mapper.CourseMapper;
import com.smartteach.modules.course.service.CourseService;
import com.smartteach.modules.course.vo.CourseDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseChapterMapper chapterMapper;
    private final CourseContentMapper contentMapper;

    public CourseServiceImpl(CourseChapterMapper chapterMapper, CourseContentMapper contentMapper) {
        this.chapterMapper = chapterMapper;
        this.contentMapper = contentMapper;
    }

    @Override
    public PageResult<Course> page(CourseQueryDTO query) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getCourseCode()), Course::getCourseCode, query.getCourseCode())
                .like(StringUtils.isNotBlank(query.getCourseName()), Course::getCourseName, query.getCourseName())
                .eq(query.getCategoryId() != null, Course::getCategoryId, query.getCategoryId())
                .eq(query.getTeacherId() != null, Course::getTeacherId, query.getTeacherId())
                .eq(query.getCourseType() != null, Course::getCourseType, query.getCourseType())
                .eq(query.getStatus() != null, Course::getStatus, query.getStatus())
                .orderByDesc(Course::getCreateTime);
        IPage<Course> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public CourseDetailVO detail(Long id) {
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        CourseDetailVO vo = new CourseDetailVO();
        vo.setCourse(course);

        List<CourseChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, id)
                .orderByAsc(CourseChapter::getSort));
        List<CourseContent> contents = contentMapper.selectList(new LambdaQueryWrapper<CourseContent>()
                .eq(CourseContent::getCourseId, id)
                .orderByAsc(CourseContent::getSort));

        Map<Long, List<CourseContent>> contentMap = contents.stream()
                .collect(Collectors.groupingBy(CourseContent::getChapterId));

        List<CourseDetailVO.ChapterNode> roots = buildChapterTree(chapters, contentMap, 0L);
        vo.setChapters(roots);
        return vo;
    }

    private List<CourseDetailVO.ChapterNode> buildChapterTree(List<CourseChapter> chapters,
                                                                Map<Long, List<CourseContent>> contentMap,
                                                                Long parentId) {
        List<CourseDetailVO.ChapterNode> result = new ArrayList<>();
        for (CourseChapter ch : chapters) {
            if (ch.getParentId().equals(parentId)) {
                CourseDetailVO.ChapterNode node = new CourseDetailVO.ChapterNode();
                node.setChapter(ch);
                node.setContents(contentMap.getOrDefault(ch.getId(), new ArrayList<>()));
                node.setChildren(buildChapterTree(chapters, contentMap, ch.getId()));
                result.add(node);
            }
        }
        result.sort(Comparator.comparing(n -> n.getChapter().getSort() == null ? 0 : n.getChapter().getSort()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(CourseSaveDTO dto) {
        boolean exists = this.lambdaQuery().eq(Course::getCourseCode, dto.getCourseCode()).exists();
        if (exists) {
            throw new BusinessException("课程编号已存在");
        }
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        if (course.getStatus() == null) {
            course.setStatus(0);
        }
        this.save(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseSaveDTO dto) {
        Course exists = this.getById(dto.getId());
        if (exists == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        this.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
        // 同步删除章节与内容
        chapterMapper.delete(new LambdaUpdateWrapper<CourseChapter>().in(CourseChapter::getCourseId, ids));
        contentMapper.delete(new LambdaUpdateWrapper<CourseContent>().in(CourseContent::getCourseId, ids));
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        this.updateById(course);
    }

    @Override
    public List<Course> listByTeacher(Long teacherId) {
        return this.lambdaQuery().eq(Course::getTeacherId, teacherId)
                .in(Course::getStatus, 0, 1)
                .orderByDesc(Course::getCreateTime)
                .list();
    }

    @Override
    public List<Course> listAllEnabled() {
        return this.lambdaQuery()
                .in(Course::getStatus, 0, 1)
                .orderByDesc(Course::getCreateTime)
                .list();
    }
}
