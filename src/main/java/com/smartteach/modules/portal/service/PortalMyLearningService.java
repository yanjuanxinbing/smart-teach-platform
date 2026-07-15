package com.smartteach.modules.portal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteach.modules.portal.vo.PortalMyAssignmentVO;
import com.smartteach.modules.portal.vo.PortalMyCourseVO;
import com.smartteach.modules.portal.vo.PortalMyTrainingVO;

/**
 * 「我的学习中心」三联端点 —— 严格使用 UserContext.getUserId() 作为数据隔离依据
 */
public interface PortalMyLearningService {

    /** 已选课程 */
    IPage<PortalMyCourseVO> myCourses(Long studentId, long pageNum, long pageSize, String keyword);

    /**
     * 学生加入课程
     * <p>前置条件：课程存在且 status=1(已发布);同一学生同一课程幂等(已选则直接返回成功)</p>
     *
     * @param studentId 学生ID(从 JWT 取)
     * @param courseId  课程ID
     * @return 选课后的 PortalMyCourseVO(含冗余字段,前端无需再请求 myCourses)
     */
    PortalMyCourseVO joinCourse(Long studentId, Long courseId);

    /** 我的作业（含提交状态） */
    IPage<PortalMyAssignmentVO> myAssignments(Long studentId, long pageNum, long pageSize, String status);

    /** 我的实训 */
    IPage<PortalMyTrainingVO> myTrainings(Long studentId, long pageNum, long pageSize, String status);
}
