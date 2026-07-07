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
import com.smartteach.modules.training.entity.TrainingPlan;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingPlanMapper;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import com.smartteach.modules.training.service.TrainingPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {

    private final TrainingRegistrationMapper registrationMapper;

    public TrainingPlanServiceImpl(TrainingRegistrationMapper registrationMapper) {
        this.registrationMapper = registrationMapper;
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
    public TrainingPlan detail(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return plan;
    }

    @Override
    public TrainingPlan save(TrainingPlanSaveDTO dto) {
        TrainingPlan plan = new TrainingPlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) plan.setStatus(0);
        this.save(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainingPlanSaveDTO dto) {
        TrainingPlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 0 && plan.getStatus() != 4) {
            throw new BusinessException("只有草稿或驳回状态才能编辑");
        }
        TrainingPlan entity = new TrainingPlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        // 先删报名记录
        registrationMapper.delete(new LambdaUpdateWrapper<TrainingRegistration>()
                .in(TrainingRegistration::getPlanId, ids));
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
            throw new BusinessException("只有草稿才能发布");
        }
        plan.setStatus(1);
        this.updateById(plan);
    }

    @Override
    public void finish(Long id) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 2) {
            throw new BusinessException("只有进行中才能完结");
        }
        plan.setStatus(3);
        this.updateById(plan);
    }

    @Override
    public void approve(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("只有已发布才能审核通过");
        }
        plan.setStatus(2);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }

    @Override
    public void reject(Long id, Long approverId, String approverName, String remark) {
        TrainingPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("只有已发布才能驳回");
        }
        plan.setStatus(4);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }
}
