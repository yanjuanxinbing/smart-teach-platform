package com.smartteach.modules.training.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrainingScoreItemDTO {
    private Long id;
    private Long registrationId;
    private String itemName;
    private BigDecimal itemScore;
    private BigDecimal maxScore;
    private BigDecimal weight;
    private String comment;
}
