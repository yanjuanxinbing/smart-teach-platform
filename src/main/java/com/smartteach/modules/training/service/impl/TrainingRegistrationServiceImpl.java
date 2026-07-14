package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import com.smartteach.modules.training.service.TrainingPlanService;
import com.smartteach.modules.training.service.TrainingRegistrationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingRegistrationServiceImpl extends ServiceImpl<TrainingRegistrationMapper, TrainingRegistration>
        implements TrainingRegistrationService {

    private final TrainingPlanService planService;

    public TrainingRegistrationServiceImpl(TrainingPlanService planService) {
        this.planService = planService;
    }

    @Override
    public PageResult<TrainingRegistration> page(String keyword, String className, Integer status, PageQuery query) {
        LambdaQueryWrapper<TrainingRegistration> wrapper = new LambdaQueryWrapper<>();
        // 按项目名称模糊搜索：通过 training_plan 表反查匹配的 planId 集合
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.inSql(TrainingRegistration::getPlanId,
                    "SELECT id FROM training_plan WHERE project_name LIKE '%" + keyword + "%'");
        }
        // 按班级搜索：班级来源于实训计划，因此通过 training_plan 表反查匹配的 planId 集合
        if (className != null && !className.isEmpty()) {
            wrapper.inSql(TrainingRegistration::getPlanId,
                    "SELECT id FROM training_plan WHERE class_name LIKE '%" + className + "%'");
        }
        wrapper.eq(status != null, TrainingRegistration::getStatus, status)
                .orderByDesc(TrainingRegistration::getCreateTime);
        IPage<TrainingRegistration> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void register(TrainingRegistrationSaveDTO dto) {
        // a) 查出关联的 TrainingPlan
        TrainingPlan plan = planService.getById(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException("实训计划不存在");
        }
        // b) 计划状态检查：只有"进行中"(status=3) 的计划才允许报名
        Integer planStatus = plan.getStatus();
        if (planStatus == null || planStatus != 3) {
            throw new BusinessException("只有进行中的实训计划才能报名");
        }
        // c) 重复报名检查
        long dupCount = this.count(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, dto.getPlanId())
                .eq(TrainingRegistration::getStudentId, dto.getStudentId()));
        if (dupCount > 0) {
            throw new BusinessException("该学生已报名此实训计划");
        }
        // d) 人数上限检查
        long count = this.count(new LambdaQueryWrapper<TrainingRegistration>()
                .eq(TrainingRegistration::getPlanId, dto.getPlanId())
                .in(TrainingRegistration::getStatus, 0, 1));
        if (plan.getCapacity() != null && count >= plan.getCapacity()) {
            throw new BusinessException("该实训计划报名人数已满");
        }
        // 保存报名记录
        TrainingRegistration reg = new TrainingRegistration();
        BeanUtils.copyProperties(dto, reg);
        // 冗余保存 planTitle，避免列表查询时再次 JOIN
        reg.setPlanTitle(plan.getPlanTitle());
        if (reg.getStatus() == null) reg.setStatus(0);
        this.save(reg);
    }

    @Override
    public void review(Long id, Integer status, String comment) {
        TrainingRegistration reg = this.getById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (reg.getStatus() != 0) {
            throw new BusinessException("当前状态不允许审核");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException("无效的审核状态");
        }
        TrainingRegistration update = new TrainingRegistration();
        update.setId(id);
        update.setStatus(status);
        update.setComment(comment);
        this.updateById(update);
    }

    @Override
    public void signIn(Long id) {
        TrainingRegistration reg = new TrainingRegistration();
        reg.setId(id);
        reg.setSignInTime(LocalDateTime.now());
        this.updateById(reg);
    }

    @Override
    public void signOut(Long id) {
        TrainingRegistration reg = new TrainingRegistration();
        reg.setId(id);
        reg.setSignOutTime(LocalDateTime.now());
        this.updateById(reg);
    }

    @Override
    public void grade(Long id, BigDecimal regularScore, BigDecimal examScore,
                      Integer regularWeight, Integer examWeight, String comment) {
        TrainingRegistration reg = this.getById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 前置状态校验：允许 1（已通过）和 3（已完成）状态重评
        if (reg.getStatus() == null || (reg.getStatus() != 1 && reg.getStatus() != 3)) {
            throw new BusinessException("只有已通过或已完成的报名才能登记成绩");
        }
        // 校验平时/考核成绩必须在 0-100
        if (regularScore != null && (regularScore.compareTo(BigDecimal.ZERO) < 0 || regularScore.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("平时成绩必须在0-100之间");
        }
        if (examScore != null && (examScore.compareTo(BigDecimal.ZERO) < 0 || examScore.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("考核成绩必须在0-100之间");
        }
        // 校验占比：必须都为非负且合计为 100
        if (regularWeight == null || examWeight == null) {
            throw new BusinessException("平时占比和考核占比都必须填写");
        }
        if (regularWeight < 0 || examWeight < 0 || regularWeight + examWeight != 100) {
            throw new BusinessException("占比必须为非负数且合计为100");
        }
        // 计算最终成绩：score = regularScore × regularWeight% + examScore × examWeight%
        BigDecimal finalScore = BigDecimal.ZERO;
        if (regularScore != null) {
            finalScore = finalScore.add(regularScore.multiply(BigDecimal.valueOf(regularWeight))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
        }
        if (examScore != null) {
            finalScore = finalScore.add(examScore.multiply(BigDecimal.valueOf(examWeight))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
        }

        TrainingRegistration update = new TrainingRegistration();
        update.setId(id);
        update.setRegularScore(regularScore);
        update.setExamScore(examScore);
        update.setRegularWeight(regularWeight);
        update.setExamWeight(examWeight);
        update.setScore(finalScore);
        update.setComment(comment);
        update.setStatus(3);
        this.updateById(update);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}