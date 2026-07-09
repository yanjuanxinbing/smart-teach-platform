package com.smartteach.modules.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.training.entity.TrainingScoreItem;
import java.math.BigDecimal;
import java.util.List;

public interface TrainingScoreItemService extends IService<TrainingScoreItem> {
    List<TrainingScoreItem> listByRegistrationId(Long registrationId);
    void saveItems(Long registrationId, List<com.smartteach.modules.training.dto.TrainingScoreItemDTO> items);
    BigDecimal calcTotalScore(Long registrationId);
}
