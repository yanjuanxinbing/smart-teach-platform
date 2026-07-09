package com.smartteach.modules.training.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
import com.smartteach.modules.training.dto.TrainingScoreItemDTO;
import com.smartteach.modules.training.entity.TrainingRegistration;
import com.smartteach.modules.training.service.TrainingRegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 实训报名/成绩管理
 */
@Api(tags = "实训计划管理-报名")
@RestController
@RequestMapping("/training/registration")
@RequiredArgsConstructor
public class TrainingRegistrationController {

    private final TrainingRegistrationService registrationService;

    @ApiOperation("分页查询报名记录")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('training:registration:list')")
    public Result<PageResult<TrainingRegistration>> page(@RequestParam(required = false) Long planId, PageQuery query) {
        return Result.success(registrationService.page(planId, query));
    }

    @ApiOperation("新增报名")
    @PostMapping
    @PreAuthorize("hasAuthority('training:registration:add')")
    @OperationLog(module = "实训报名", action = "新增报名", saveParams = false)
    public Result<Void> add(@Valid @RequestBody TrainingRegistrationSaveDTO dto) {
        registrationService.register(dto);
        return Result.success();
    }

    @ApiOperation("审核报名")
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAuthority('training:registration:review')")
    @OperationLog(module = "实训报名", action = "审核报名", saveParams = false)
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String comment = (String) body.get("comment");
        registrationService.review(id, status, comment);
        return Result.success();
    }

    @ApiOperation("登记成绩")
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('training:registration:grade')")
    @OperationLog(module = "实训报名", action = "登记成绩", saveParams = false)
    public Result<Void> grade(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal score = new BigDecimal(body.get("score").toString());
        String comment = (String) body.get("comment");
        registrationService.grade(id, score, comment);
        return Result.success();
    }

    @ApiOperation("多维评分")
    @PutMapping("/{id}/grade-items")
    @PreAuthorize("hasAuthority('training:registration:grade')")
    @OperationLog(module = "实训报名", action = "多维评分", saveParams = false)
    public Result<Void> gradeItems(@PathVariable Long id, @RequestBody List<TrainingScoreItemDTO> items) {
        registrationService.gradeWithItems(id, items);
        return Result.success();
    }

    @ApiOperation("批量删除报名记录")
    @DeleteMapping
    @PreAuthorize("hasAuthority('training:registration:remove')")
    @OperationLog(module = "实训报名", action = "删除报名记录", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        registrationService.remove(ids);
        return Result.success();
    }
}
