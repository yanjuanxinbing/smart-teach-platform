package com.smartteach.modules.aiassignment.service;

import com.smartteach.modules.aiassignment.dto.AnalyticsGenerateRequestDTO;
import com.smartteach.modules.aiassignment.vo.AiAnalyticsReportVO;
import com.smartteach.modules.aiassignment.vo.ClassAssignmentStatsVO;
import com.smartteach.modules.aiassignment.vo.StudentAssignmentStatsVO;

/**
 * aiassignment 模块对外业务接口
 *  - 班级维度统计 / AI 报告（教师）
 *  - 学生维度统计 / AI 报告（学生）
 */
public interface AssignmentAnalyticsService {

    ClassAssignmentStatsVO classStats(Long classId, String semester);

    StudentAssignmentStatsVO studentStats(Long studentId, String semester);

    AiAnalyticsReportVO generateClassReport(AnalyticsGenerateRequestDTO dto);

    AiAnalyticsReportVO generateStudentReport(AnalyticsGenerateRequestDTO dto);
}
