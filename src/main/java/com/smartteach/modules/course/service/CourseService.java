package com.smartteach.modules.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.course.dto.CourseQueryDTO;
import com.smartteach.modules.course.dto.CourseSaveDTO;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.vo.CourseDetailVO;

import java.util.List;

public interface CourseService extends IService<Course> {

    PageResult<Course> page(CourseQueryDTO query);

    CourseDetailVO detail(Long id);

    void save(CourseSaveDTO dto);

    void update(CourseSaveDTO dto);

    void remove(List<Long> ids);

    void changeStatus(Long id, Integer status);

    List<Course> listByTeacher(Long teacherId);

    List<Course> listAllEnabled();
}
