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

    /** 我的作业（含提交状态） */
    IPage<PortalMyAssignmentVO> myAssignments(Long studentId, long pageNum, long pageSize, String status);

    /** 我的实训 */
    IPage<PortalMyTrainingVO> myTrainings(Long studentId, long pageNum, long pageSize, String status);
}
