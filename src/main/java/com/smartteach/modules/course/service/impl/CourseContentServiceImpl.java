package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.modules.course.dto.CourseContentSaveDTO;
import com.smartteach.modules.course.entity.CourseContent;
import com.smartteach.modules.course.mapper.CourseContentMapper;
import com.smartteach.modules.course.service.CourseContentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseContentServiceImpl extends ServiceImpl<CourseContentMapper, CourseContent> implements CourseContentService {

    @Override
    public List<CourseContent> listByChapter(Long chapterId) {
        return this.lambdaQuery()
                .eq(CourseContent::getChapterId, chapterId)
                .orderByAsc(CourseContent::getSort)
                .list();
    }

    @Override
    public List<CourseContent> listByCourse(Long courseId) {
        return this.lambdaQuery()
                .eq(CourseContent::getCourseId, courseId)
                .orderByAsc(CourseContent::getSort)
                .list();
    }

    @Override
    public void save(CourseContentSaveDTO dto) {
        CourseContent content = new CourseContent();
        BeanUtils.copyProperties(dto, content);
        if (content.getStatus() == null) {
            content.setStatus(1);
        }
        this.save(content);
    }

    @Override
    public void update(CourseContentSaveDTO dto) {
        CourseContent content = new CourseContent();
        BeanUtils.copyProperties(dto, content);
        this.updateById(content);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}
