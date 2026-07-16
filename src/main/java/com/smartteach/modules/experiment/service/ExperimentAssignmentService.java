package com.smartteach.modules.experiment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.experiment.entity.ExperimentAssignment;

import java.math.BigDecimal;
import java.util.List;

public interface ExperimentAssignmentService extends IService<ExperimentAssignment> {

    /** 分页查询分配记录 */
    PageResult<ExperimentAssignment> page(String keyword, String className, Integer status, PageQuery query);

    /**
     * 按班级分配：自动给该班每个学生铺一条 status=1 的记录
     * @return 实际新增条数（不含已存在的）
     */
    int assignByClass(Long planId, String className, Long operatorId);

    /**
     * 实验计划保存后被自动调用：给定一份刚保存/更新的实验计划，按它的 className 给班里每个学生
     * 铺一条 status=1 的评分记录。空 className 或找不到班级/班级无成员时为 noop。
     * 与 {@link #assignByClass} 的差别：本方法不做 status=1 校验，仅做幂等去重。
     *
     * @return 实际新增条数
     */
    int autoCreateByPlan(com.smartteach.modules.experiment.entity.ExperimentPlan plan, Long operatorId);

    /** 单条分配：手动指定单个学生 */
    ExperimentAssignment assignOne(Long planId, Long studentId, Long operatorId);

    /** 撤销分配（软删） */
    void remove(List<Long> ids);

    /** 标记完成：录成绩 + 状态置 3 */
    void markCompleted(Long id, BigDecimal score, String comment);
}