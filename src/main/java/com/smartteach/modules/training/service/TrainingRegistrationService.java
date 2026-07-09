package com.smartteach.modules.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.base.PageQuery;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
import com.smartteach.modules.training.entity.TrainingRegistration;

import java.util.List;

public interface TrainingRegistrationService extends IService<TrainingRegistration> {

    PageResult<TrainingRegistration> page(Long planId, PageQuery query);

    void register(TrainingRegistrationSaveDTO dto);

    void review(Long id, Integer status, String comment);

    void signIn(Long id);

    void signOut(Long id);

    void grade(Long id, java.math.BigDecimal score, String comment);

    void remove(List<Long> ids);
}
