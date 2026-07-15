package com.smartteach.modules.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.course.dto.CourseTeacherAssignDTO;
import com.smartteach.modules.course.dto.CourseTeacherQueryDTO;
import com.smartteach.modules.course.entity.CourseTeacher;
import com.smartteach.modules.course.vo.CourseTeacherVO;
import com.smartteach.modules.user.vo.UserVO;

import java.util.List;

public interface CourseTeacherService extends IService<CourseTeacher> {

    /** 分页查询授课列表（含课程名、教师名等冗余字段） */
    PageResult<CourseTeacherVO> page(CourseTeacherQueryDTO query);

    /** 全量替换一门课程的授课关系（先删后插） */
    void assign(CourseTeacherAssignDTO dto);

    /** 状态切换 */
    void changeStatus(Long id, Integer status);

    /** 批量软删 */
    void remove(List<Long> ids);

    /** 查询某课程的所有授课教师（带 UserVO 信息） */
    List<UserVO> listTeachersByCourseId(Long courseId);

    /** 列出某教师的所有课程-教师关系行（不直接返回 Course） */
    List<CourseTeacher> listByTeacherId(Long teacherId);
}