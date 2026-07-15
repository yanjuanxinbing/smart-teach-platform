package com.smartteach.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据教师ID查出该教师任课的所有启用的课程。
     * 用于 /course/manage/my（授课管理上线后，myCourses 走 JOIN，不再用 course.teacher_id 单 FK）。
     */
    List<Course> selectCoursesByTeacherId(@Param("teacherId") Long teacherId);
}