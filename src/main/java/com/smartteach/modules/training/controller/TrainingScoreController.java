package com.smartteach.modules.training.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.training.dto.TrainingScoreItemDTO;
import com.smartteach.modules.training.entity.TrainingScoreItem;
import com.smartteach.modules.training.service.TrainingScoreItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Api(tags = "实训成绩明细")
@RestController
@RequestMapping("/training/score")
@RequiredArgsConstructor
public class TrainingScoreController {

    private final TrainingScoreItemService scoreItemService;

    @ApiOperation("查询成绩明细列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('training:registration:list')")
    public Result<List<TrainingScoreItem>> list(@RequestParam Long registrationId) {
        return Result.success(scoreItemService.listByRegistrationId(registrationId));
    }

    @ApiOperation("保存成绩明细")
    @PostMapping("/{registrationId}")
    @PreAuthorize("hasAuthority('training:registration:grade')")
    public Result<BigDecimal> save(@PathVariable Long registrationId,
                                    @Valid @RequestBody List<TrainingScoreItemDTO> items) {
        scoreItemService.saveItems(registrationId, items);
        BigDecimal totalScore = scoreItemService.calcTotalScore(registrationId);
        return Result.success(totalScore);
    }
}
