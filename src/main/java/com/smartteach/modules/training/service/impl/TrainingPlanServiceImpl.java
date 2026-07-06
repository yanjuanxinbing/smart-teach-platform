package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.training.dto.TrainingPlanQueryDTO;
import com.smartteach.modules.training.dto.TrainingPlanSaveDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.mapper.TrainingPlanMapper;
import com.smartteach.modules.training.service.TrainingPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {

    @Override
    public PageResult<TrainingPlan> page(TrainingPlanQueryDTO query) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getPlanTitle()), TrainingPlan::getPlanTitle, query.getPlanTitle())
                .like(StringUtils.isNotBlank(query.getProjectName()), TrainingPlan::getProjectName, query.getProjectName())
                .eq(StringUtils.isNotBlank(query.getSemester()), TrainingPlan::getSemester, query.getSemester())
                .like(StringUtils.isNotBlank(query.getClassName()), TrainingPlan::getClassName, query.getClassName())
                .eq(query.getTeacherId() != null, TrainingPlan::getTeacherId, query.getTeacherId())
                .eq(query.getStatus() != null, TrainingPlan::getStatus, query.getStatus())
                .orderByDesc(TrainingPlan::getCreateTime);
        IPage<TrainingPlan> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public TrainingPlan detail(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return plan;
    }

    @Override
    public void save(TrainingPlanSaveDTO dto) {
        TrainingPlan plan = new TrainingPlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) plan.setStatus(0);
        this.save(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainingPlanSaveDTO dto) {
        TrainingPlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        TrainingPlan entity = new TrainingPlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public void publish(Long id) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(1);
        this.updateById(plan);
    }

    @Override
    public void finish(Long id) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(3);
        this.updateById(plan);
    }

    @Override
    public void approve(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(2);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }

    @Override
    public void reject(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(4);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }
}
