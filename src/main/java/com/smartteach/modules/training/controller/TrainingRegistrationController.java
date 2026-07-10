package com.smartteach.modules.training.controller;

import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import com.smartteach.modules.training.dto.TrainingRegistrationSaveDTO;
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
 * 实训报名/签到/成绩管理
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
    public Result<PageResult<TrainingRegistration>> page(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String className,
                                                        @RequestParam(required = false) Integer status,
                                                        PageQuery query) {
        return Result.success(registrationService.page(keyword, className, status, query));
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

    @ApiOperation("签到")
    @PutMapping("/{id}/sign-in")
    @OperationLog(module = "实训报名", action = "签到", saveParams = false)
    public Result<Void> signIn(@PathVariable Long id) {
        registrationService.signIn(id);
        return Result.success();
    }

    @ApiOperation("签退")
    @PutMapping("/{id}/sign-out")
    @OperationLog(module = "实训报名", action = "签退", saveParams = false)
    public Result<Void> signOut(@PathVariable Long id) {
        registrationService.signOut(id);
        return Result.success();
    }

    @ApiOperation("登记成绩")
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAuthority('training:registration:grade')")
    @OperationLog(module = "实训报名", action = "登记成绩", saveParams = false)
    public Result<Void> grade(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 先把原始值取出来再做类型转换，避免在三元里同时出现 map.get(...).toString() 的潜在 NPE
        Object rScoreObj = body.get("regularScore");
        Object eScoreObj = body.get("examScore");
        Object rWeightObj = body.get("regularWeight");
        Object eWeightObj = body.get("examWeight");

        BigDecimal regularScore = rScoreObj == null ? null : toBigDecimal(rScoreObj);
        BigDecimal examScore = eScoreObj == null ? null : toBigDecimal(eScoreObj);
        Integer regularWeight = rWeightObj == null ? null : toInteger(rWeightObj);
        Integer examWeight = eWeightObj == null ? null : toInteger(eWeightObj);
        String comment = body.get("comment") == null ? null : body.get("comment").toString();

        registrationService.grade(id, regularScore, examScore, regularWeight, examWeight, comment);
        return Result.success();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return new BigDecimal(o.toString());
        return new BigDecimal(o.toString());
    }

    private static Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.valueOf(o.toString());
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
