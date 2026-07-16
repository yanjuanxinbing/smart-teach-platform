package com.smartteach.modules.aiassignment.mapper;

import com.smartteach.modules.aiassignment.vo.AssignmentAvgScoreVO;
import com.smartteach.modules.aiassignment.vo.MonthlyPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * aiassignment 模块聚合查询 —— 全部走 XML，便于写较复杂的 GROUP BY / CASE WHEN。
 */
@Mapper
public interface AnalyticsMapper {

    /** 班级总览：assignment_count / expected / submitted / late_count */
    Map<String, Object> classOverview(@Param("classId") Long classId,
                                       @Param("startDate") LocalDateTime start,
                                       @Param("endDate") LocalDateTime end);

    /** 班级已批改平均分（仅看 status=2 的行） */
    Double classAvgScore(@Param("classId") Long classId,
                         @Param("startDate") LocalDateTime start,
                         @Param("endDate") LocalDateTime end);

    /** 班级分数段 5 个 SUM 一次返回（key: b90 / b80 / b70 / b60 / bFail） */
    Map<String, Object> classScoreBuckets(@Param("classId") Long classId,
                                          @Param("startDate") LocalDateTime start,
                                          @Param("endDate") LocalDateTime end);

    /** 班级应提交总人次（作业数 × 班级学生数） */
    Long classExpectedSubmissions(@Param("classId") Long classId,
                                  @Param("startDate") LocalDateTime start,
                                  @Param("endDate") LocalDateTime end);

    /** 班级实际提交人次（status IN (1,2)） */
    Long classSubmittedCount(@Param("classId") Long classId,
                             @Param("startDate") LocalDateTime start,
                             @Param("endDate") LocalDateTime end);

    /** 班级按作业平均分列表（按 deadline asc） */
    List<AssignmentAvgScoreVO> classAssignmentAvgScores(@Param("classId") Long classId,
                                                        @Param("startDate") LocalDateTime start,
                                                        @Param("endDate") LocalDateTime end);

    /** 班级按月时间序列 */
    List<MonthlyPointVO> classMonthlyTrend(@Param("classId") Long classId,
                                           @Param("startDate") LocalDateTime start,
                                           @Param("endDate") LocalDateTime end);

    /** 学生总览：assignment/submitted/graded/avg/max/min/std/late */
    Map<String, Object> studentOverview(@Param("studentId") Long studentId,
                                        @Param("startDate") LocalDateTime start,
                                        @Param("endDate") LocalDateTime end);

    /** 学生分数随时间点（status=2，仅看已批改） */
    List<com.smartteach.modules.aiassignment.vo.StudentScorePointVO> studentScoreTrend(@Param("studentId") Long studentId,
                                                                                       @Param("startDate") LocalDateTime start,
                                                                                       @Param("endDate") LocalDateTime end);

    /** 学生未提交清单（含 status=0 草稿） */
    List<com.smartteach.modules.aiassignment.vo.UnsubmittedAssignmentVO> studentUnsubmitted(@Param("studentId") Long studentId,
                                                                                            @Param("startDate") LocalDateTime start,
                                                                                            @Param("endDate") LocalDateTime end);
}
