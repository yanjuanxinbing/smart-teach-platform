package com.smartteach.modules.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.course.dto.CourseChapterSaveDTO;
import com.smartteach.modules.course.entity.CourseChapter;

import java.util.List;

public interface CourseChapterService extends IService<CourseChapter> {

    List<CourseChapter> listByCourse(Long courseId);

    void save(CourseChapterSaveDTO dto);

    void update(CourseChapterSaveDTO dto);

    void remove(Long id);
}
