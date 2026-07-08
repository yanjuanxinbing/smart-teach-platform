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
    public PageResult<TrainingRegistration> page(Long planId, PageQuery query) {
        LambdaQueryWrapper<TrainingRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, TrainingRegistration::getPlanId, planId)
                .like(query.getKeyword() != null, TrainingRegistration::getStudentName, query.getKeyword())
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
        // b) 计划状态检查：status 必须在 [1,2,3]
        Integer planStatus = plan.getStatus();
        if (planStatus == null || (planStatus != 1 && planStatus != 2 && planStatus != 3)) {
            throw new BusinessException("该计划当前不允许报名");
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
    public void grade(Long id, BigDecimal score, String comment) {
        TrainingRegistration reg = this.getById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // a) score 范围校验
        if (score != null && (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("成绩必须在0-100之间");
        }
        // b) 前置状态校验
        if (reg.getStatus() == null || reg.getStatus() != 1) {
            throw new BusinessException("只有已通过的报名才能登记成绩");
        }
        TrainingRegistration update = new TrainingRegistration();
        update.setId(id);
        update.setScore(score);
        update.setComment(comment);
        update.setStatus(3);
        this.updateById(update);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}