package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.training.dto.TrainingPlanQueryDTO;
import com.smartteach.modules.training.dto.TrainingPlanSaveDTO;
import com.smartteach.modules.training.dto.TrainingPlanStageDTO;
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingPlanStage;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingPlanMapper;
import com.smartteach.modules.training.mapper.TrainingPlanStageMapper;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import com.smartteach.modules.training.service.TrainingPlanService;
import com.smartteach.modules.training.vo.TrainingPlanDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {

    private final TrainingRegistrationMapper registrationMapper;
    private final TrainingPlanStageMapper stageMapper;

    public TrainingPlanServiceImpl(TrainingRegistrationMapper registrationMapper,
                                   TrainingPlanStageMapper stageMapper) {
        this.registrationMapper = registrationMapper;
        this.stageMapper = stageMapper;
    }

    @Override
    public PageResult<TrainingPlan> page(TrainingPlanQueryDTO query) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getPlanTitle()), TrainingPlan::getPlanTitle, query.getPlanTitle())
                .like(StringUtils.isNotBlank(query.getProjectName()), TrainingPlan::getProjectName, query.getProjectName())
                .eq(StringUtils.isNotBlank(query.getSemester()), TrainingPlan::getSemester, query.getSemester())
                .like(StringUtils.isNotBlank(query.getClassName()), TrainingPlan::getClassName, query.getClassName())
                .eq(query.getTeacherId() != null, TrainingPlan::getTeacherId, query.getTeacherId())
                .eq(query.getStatus() != null, TrainingPlan::getStatus, query.getStatus())
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w
                        .like(TrainingPlan::getPlanTitle, query.getKeyword())
                        .or().like(TrainingPlan::getProjectName, query.getKeyword()))
                .orderByDesc(TrainingPlan::getCreateTime);
        IPage<TrainingPlan> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public TrainingPlanDetailVO detail(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        List<TrainingPlanStage> stages = stageMapper.selectList(new LambdaQueryWrapper<TrainingPlanStage>()
                .eq(TrainingPlanStage::getPlanId, id)
                .orderByAsc(TrainingPlanStage::getSortNo)
                .orderByAsc(TrainingPlanStage::getStartDate));
        TrainingPlanDetailVO vo = new TrainingPlanDetailVO();
        vo.setPlan(plan);
        vo.setStages(stages);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlan save(TrainingPlanSaveDTO dto) {
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        TrainingPlan plan = new TrainingPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setDurationDays(computeDurationDays(dto.getStartDate(), dto.getEndDate()));
        if (plan.getStatus() == null) plan.setStatus(0);
        this.save(plan);
        saveStages(plan.getId(), dto.getStages());
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainingPlanSaveDTO dto) {
        TrainingPlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        TrainingPlan entity = new TrainingPlan();
        BeanUtils.copyProperties(dto, entity);
        entity.setDurationDays(computeDurationDays(dto.getStartDate(), dto.getEndDate()));
        // 如果当前状态是已驳回(5)，编辑后改为草稿(0)
        if (plan.getStatus() == 5) {
            entity.setStatus(0);
        }
        this.updateById(entity);
        // 重新保存阶段
        stageMapper.delete(new LambdaUpdateWrapper<TrainingPlanStage>().eq(TrainingPlanStage::getPlanId, dto.getId()));
        saveStages(dto.getId(), dto.getStages());
    }

    private void saveStages(Long planId, List<TrainingPlanStageDTO> stages) {
        if (stages == null || stages.isEmpty()) {
            return;
        }
        List<TrainingPlanStage> list = stages.stream().map(s -> {
            TrainingPlanStage entity = new TrainingPlanStage();
            BeanUtils.copyProperties(s, entity);
            entity.setId(null);
            entity.setPlanId(planId);
            if (entity.getDurationDays() == null) {
                entity.setDurationDays(computeDurationDays(entity.getStartDate(), entity.getEndDate()));
            }
            return entity;
        }).collect(Collectors.toList());
        list.forEach(stageMapper::insert);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) {
            throw new BusinessException("开始/结束日期不能为空");
        }
        if (end.isBefore(start)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }

    private Integer computeDurationDays(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) return null;
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        return (int) Math.max(days, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        // 先删报名记录
        registrationMapper.delete(new LambdaUpdateWrapper<TrainingRegistration>()
                .in(TrainingRegistration::getPlanId, ids));
        // 再删阶段
        stageMapper.delete(new LambdaUpdateWrapper<TrainingPlanStage>()
                .in(TrainingPlanStage::getPlanId, ids));
        // 再删计划
        this.removeByIds(ids);
    }

    @Override
    public void publish(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 0) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(1);
        this.updateById(update);
    }

    @Override
    public void submitForReview(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(2);
        this.updateById(update);
    }

    @Override
    public void approve(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 2) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(3);
        update.setApproverId(approverId);
        update.setApproverName(approverName);
        update.setApproveRemark(remark);
        this.updateById(update);
    }

    @Override
    public void reject(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 2) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(5);
        update.setApproverId(approverId);
        update.setApproverName(approverName);
        update.setApproveRemark(remark);
        this.updateById(update);
    }

    @Override
    public void finish(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 3) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(4);
        this.updateById(update);
    }

    @Override
    public void revertToDraft(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 5) {
            throw new BusinessException("当前状态不允许此操作");
        }
        TrainingPlan update = new TrainingPlan();
        update.setId(id);
        update.setStatus(0);
        update.setApproverId(null);
        update.setApproverName(null);
        update.setApproveRemark(null);
        this.updateById(update);
    }
}
