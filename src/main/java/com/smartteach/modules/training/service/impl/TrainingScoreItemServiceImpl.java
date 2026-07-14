package com.smartteach.modules.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.modules.training.dto.TrainingScoreItemDTO;
import com.smartteach.modules.training.entity.TrainingScoreItem;
import com.smartteach.modules.training.mapper.TrainingScoreItemMapper;
import com.smartteach.modules.training.service.TrainingScoreItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingScoreItemServiceImpl extends ServiceImpl<TrainingScoreItemMapper, TrainingScoreItem>
        implements TrainingScoreItemService {

    @Override
    public List<TrainingScoreItem> listByRegistrationId(Long registrationId) {
        return this.list(new LambdaQueryWrapper<TrainingScoreItem>()
                .eq(TrainingScoreItem::getRegistrationId, registrationId)
                .orderByAsc(TrainingScoreItem::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveItems(Long registrationId, List<TrainingScoreItemDTO> items) {
        this.remove(new LambdaUpdateWrapper<TrainingScoreItem>()
                .eq(TrainingScoreItem::getRegistrationId, registrationId));
        if (items == null || items.isEmpty()) return;
        List<TrainingScoreItem> list = items.stream().map(dto -> {
            TrainingScoreItem entity = new TrainingScoreItem();
            BeanUtils.copyProperties(dto, entity);
            entity.setId(null);
            entity.setRegistrationId(registrationId);
            return entity;
        }).collect(Collectors.toList());
        list.forEach(this::save);
    }

    @Override
    public BigDecimal calcTotalScore(Long registrationId) {
        List<TrainingScoreItem> items = listByRegistrationId(registrationId);
        if (items.isEmpty()) return BigDecimal.ZERO;

        // weight 现在按"百分比"存储：30 表示 30%；<1 时按倍数（旧数据）
        BigDecimal totalWeight = items.stream()
                .map(i -> normalizeWeight(i.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal weightedSum = items.stream()
                .map(i -> {
                    BigDecimal score = i.getItemScore() != null ? i.getItemScore() : BigDecimal.ZERO;
                    BigDecimal weight = normalizeWeight(i.getWeight());
                    BigDecimal max = i.getMaxScore() != null && i.getMaxScore().compareTo(BigDecimal.ZERO) != 0
                            ? i.getMaxScore() : BigDecimal.valueOf(100);
                    // 每项贡献 = 分数/满分 * weight(百分比)
                    return score.multiply(weight)
                            .divide(max, 4, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总分 = Σ(分数/满分 * weight%) / Σ(weight%) * 100
        return weightedSum.multiply(BigDecimal.valueOf(100))
                .divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    /**
     * weight 字段语义统一为百分比数字（如 30 = 30%）。
     * 为兼容旧数据：weight <= 1 时视为倍数（1.0 = 100%），自动 *100 转成百分比。
     */
    private BigDecimal normalizeWeight(BigDecimal weight) {
        if (weight == null) return BigDecimal.valueOf(100);
        if (weight.compareTo(BigDecimal.ONE) <= 0) {
            return weight.multiply(BigDecimal.valueOf(100));
        }
        return weight;
    }
}
