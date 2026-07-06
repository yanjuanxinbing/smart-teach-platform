package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.course.dto.CourseChapterSaveDTO;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.entity.CourseContent;
import com.smartteach.modules.course.mapper.CourseChapterMapper;
import com.smartteach.modules.course.mapper.CourseContentMapper;
import com.smartteach.modules.course.service.CourseChapterService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {

    private final CourseContentMapper contentMapper;

    public CourseChapterServiceImpl(CourseContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    @Override
    public List<CourseChapter> listByCourse(Long courseId) {
        return this.lambdaQuery()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSort)
                .list();
    }

    @Override
    public void save(CourseChapterSaveDTO dto) {
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(dto, chapter);
        if (chapter.getParentId() == null) {
            chapter.setParentId(0L);
        }
        this.save(chapter);
    }

    @Override
    public void update(CourseChapterSaveDTO dto) {
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(dto, chapter);
        this.updateById(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        boolean hasChildren = this.lambdaQuery().eq(CourseChapter::getParentId, id).exists();
        if (hasChildren) {
            throw new BusinessException("请先删除子章节");
        }
        this.removeById(id);
        // 同步删除章节下的内容
        contentMapper.delete(new LambdaQueryWrapper<CourseContent>().eq(CourseContent::getChapterId, id));
    }
}
