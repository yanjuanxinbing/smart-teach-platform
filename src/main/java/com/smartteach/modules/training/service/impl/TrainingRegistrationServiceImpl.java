package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.mapper.TrainingRegistrationMapper;
import com.smartteach.modules.training.service.TrainingRegistrationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingRegistrationServiceImpl extends ServiceImpl<TrainingRegistrationMapper, TrainingRegistration>
        implements TrainingRegistrationService {

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
        TrainingRegistration reg = new TrainingRegistration();
        BeanUtils.copyProperties(dto, reg);
        if (reg.getStatus() == null) reg.setStatus(0);
        this.save(reg);
    }

    @Override
    public void review(Long id, Integer status, String comment) {
        TrainingRegistration reg = new TrainingRegistration();
        reg.setId(id);
        reg.setStatus(status);
        reg.setComment(comment);
        this.updateById(reg);
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
    public void grade(Long id, java.math.BigDecimal score, String comment) {
        TrainingRegistration reg = new TrainingRegistration();
        reg.setId(id);
        reg.setScore(score);
        reg.setComment(comment);
        reg.setStatus(3);
        this.updateById(reg);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }
}
