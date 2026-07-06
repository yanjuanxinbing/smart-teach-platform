package com.smartteach.modules.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.course.dto.CourseContentSaveDTO;
import com.smartteach.modules.course.entity.CourseContent;

import java.util.List;

public interface CourseContentService extends IService<CourseContent> {

    List<CourseContent> listByChapter(Long chapterId);

    List<CourseContent> listByCourse(Long courseId);

    void save(CourseContentSaveDTO dto);

    void update(CourseContentSaveDTO dto);

    void remove(List<Long> ids);
}
