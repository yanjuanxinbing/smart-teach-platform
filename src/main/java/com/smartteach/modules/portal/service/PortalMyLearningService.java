package com.smartteach.modules.portal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteach.modules.portal.vo.PortalExperimentDetailVO;
import com.smartteach.modules.portal.vo.PortalMyAssignmentVO;
import com.smartteach.modules.portal.vo.PortalMyCourseVO;
import com.smartteach.modules.portal.vo.PortalMyExperimentVO;
import com.smartteach.modules.portal.vo.PortalMyTrainingVO;
import com.smartteach.modules.portal.vo.PortalTrainingVO;

import java.util.List;

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

    /**
     * 学生可报名的实训计划列表（status=3 进行中），含当前学生是否已报名、当前报名状态。
     */
    List<PortalTrainingVO> listAvailableTrainings(Long studentId);

    /**
     * 门户侧-实训计划详情：返回完整字段 + 当前学生报名状态。
     * <p>不做状态过滤；任何登录学生都可访问任意 planId，前端用 {@code registered} 字段决定是否显示报名按钮</p>
     */
    PortalTrainingVO getTrainingDetail(Long studentId, Long planId);

    /**
     * 学生门户自报名 —— 落 status=0(待审核)，由管理员后续审核。
     * <p>前置条件：计划 status=3 进行中、未超过 capacity、未重复报名；studentId/姓名/电话/班级自动填充</p>
     *
     * @return 落库后的报名视图
     */
    PortalTrainingVO registerForTraining(Long studentId, Long planId);

    /**
     * 我的实验 —— 学生只看到自己被分配的实验，按 item.classDate 推算 not_started/in_progress/done
     */
    IPage<PortalMyExperimentVO> myExperiments(Long studentId, long pageNum, long pageSize, String status, String keyword);

    /**
     * 实验详情 —— 单条 item 全字段 + 所属 plan 上下文 + 当前学生 assignment 状态
     */
    PortalExperimentDetailVO getExperimentDetail(Long studentId, Long itemId);

    /**
     * 校验学生是否有权访问指定作业（基于"目标班级 ⊇ 学生班级"）。
     * 学生班级与作业目标班级任一相交即视为可见；否则抛 BusinessException(403)。
     */
    void assertAssignmentVisibleToStudent(Long assignmentId, Long studentId);
}
