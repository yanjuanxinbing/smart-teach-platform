package com.smartteach.modules.training.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_score_item")
public class TrainingScoreItem extends BaseEntity {
    private Long registrationId;
    private String itemName;
    private BigDecimal itemScore;
    private BigDecimal maxScore;
    private BigDecimal weight;
    private String comment;
}
