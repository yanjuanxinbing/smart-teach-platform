package com.smartteach.modules.portal.controller;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.portal.dto.PortalTrainingRegisterDTO;
import com.smartteach.modules.portal.service.PortalMyLearningService;
import com.smartteach.modules.portal.vo.PortalTrainingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 门户-实训计划浏览与学生自报名
 *
 * <p>与 {@link PortalMyLearningController}（/portal/my/* 我的数据）并列；
 * 本控制器走 /portal/training/* 命名空间，语义是"浏览/报名实训计划"，
 * 不属于"我的"维度的数据。</p>
 *
 * <p>studentId 仍严格从 JWT 取（反 IDOR），请求体里不接受同名字段</p>
 */
@Api(tags = "门户-实训计划浏览与报名")
@RestController
@RequestMapping("/portal/training")
@RequiredArgsConstructor
public class PortalTrainingController {

    private final PortalMyLearningService myLearningService;

    @ApiOperation("可报名的实训计划列表（status=3 进行中），含当前学生报名上下文")
    @GetMapping("/available")
    @PreAuthorize("hasAuthority('training:my:list')")
    public Result<List<PortalTrainingVO>> available() {
        Long studentId = requireStudent();
        return Result.success(myLearningService.listAvailableTrainings(studentId));
    }

    @ApiOperation("实训计划详情（任意 planId；含当前学生报名上下文）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('training:my:list')")
    public Result<PortalTrainingVO> detail(@PathVariable("id") Long id) {
        Long studentId = requireStudent();
        return Result.success(myLearningService.getTrainingDetail(studentId, id));
    }

    @ApiOperation("学生自报名（落 status=0 待审核；studentId 从 JWT 取，反 IDOR）")
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('training:my:list')")
    public Result<PortalTrainingVO> register(@Valid @RequestBody PortalTrainingRegisterDTO dto) {
        Long studentId = requireStudent();
        return Result.success(myLearningService.registerForTraining(studentId, dto.getPlanId()));
    }

    private Long requireStudent() {
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return studentId;
    }
}