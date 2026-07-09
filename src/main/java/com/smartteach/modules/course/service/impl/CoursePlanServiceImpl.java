package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.enums.PlanStatus;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.dto.CoursePlanItemDTO;
import com.smartteach.modules.course.dto.CoursePlanQueryDTO;
import com.smartteach.modules.course.dto.CoursePlanSaveDTO;
import com.smartteach.modules.course.entity.CoursePlan;
import com.smartteach.modules.course.entity.CoursePlanItem;
import com.smartteach.modules.course.mapper.CoursePlanItemMapper;
import com.smartteach.modules.course.mapper.CoursePlanMapper;
import com.smartteach.modules.course.service.CoursePlanService;
import com.smartteach.modules.course.vo.CoursePlanDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoursePlanServiceImpl extends ServiceImpl<CoursePlanMapper, CoursePlan> implements CoursePlanService {

    private final CoursePlanItemMapper itemMapper;

    public CoursePlanServiceImpl(CoursePlanItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public PageResult<CoursePlan> page(CoursePlanQueryDTO query) {
        IPage<CoursePlan> page = this.page(new Page<CoursePlan>(query.getPageNum(), query.getPageSize()), buildWrapper(query));
        return PageResult.of(page);
    }

    private LambdaQueryWrapper<CoursePlan> buildWrapper(CoursePlanQueryDTO query) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getPlanTitle()), CoursePlan::getPlanTitle, query.getPlanTitle())
                .eq(query.getCourseId() != null, CoursePlan::getCourseId, query.getCourseId())
                .eq(StringUtils.isNotBlank(query.getSemester()), CoursePlan::getSemester, query.getSemester())
                .like(StringUtils.isNotBlank(query.getClassName()), CoursePlan::getClassName, query.getClassName())
                .eq(query.getStatus() != null, CoursePlan::getStatus, query.getStatus());
        Long ownerId = Boolean.TRUE.equals(query.getMine()) ? UserContext.getUserId() : query.getTeacherId();
        wrapper.eq(ownerId != null, CoursePlan::getCreateBy, ownerId);
        wrapper.orderByDesc(CoursePlan::getCreateTime);
        return wrapper;
    }

    @Override
    public CoursePlanDetailVO detail(Long id) {
        CoursePlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        List<CoursePlanItem> items = itemMapper.selectList(new LambdaQueryWrapper<CoursePlanItem>()
                .eq(CoursePlanItem::getPlanId, id)
                .orderByAsc(CoursePlanItem::getWeekNo));
        CoursePlanDetailVO vo = new CoursePlanDetailVO();
        vo.setPlan(plan);
        vo.setItems(items);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoursePlan save(CoursePlanSaveDTO dto) {
        CoursePlan plan = new CoursePlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) {
            plan.setStatus(PlanStatus.DRAFT.getCode());
        }
        this.save(plan);
        saveItems(plan.getId(), dto.getItems());
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CoursePlanSaveDTO dto) {
        CoursePlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Integer cur = plan.getStatus();
        if (cur != null && (cur == PlanStatus.COMPLETED.getCode()
                || cur == PlanStatus.PUBLISHED.getCode()
                || cur == PlanStatus.PENDING.getCode())) {
            throw new BusinessException("当前状态（" + PlanStatus.labelOf(cur) + "）不允许编辑");
        }
        CoursePlan entity = new CoursePlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
        itemMapper.delete(new LambdaUpdateWrapper<CoursePlanItem>().eq(CoursePlanItem::getPlanId, dto.getId()));
        saveItems(dto.getId(), dto.getItems());
    }

    private void saveItems(Long planId, List<CoursePlanItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<CoursePlanItem> list = items.stream().map(item -> {
            CoursePlanItem entity = new CoursePlanItem();
            BeanUtils.copyProperties(item, entity);
            entity.setId(null);
            entity.setPlanId(planId);
            return entity;
        }).collect(Collectors.toList());
        list.forEach(itemMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        long locked = this.lambdaQuery()
                .in(CoursePlan::getId, ids)
                .in(CoursePlan::getStatus, PlanStatus.PENDING.getCode(),
                        PlanStatus.PUBLISHED.getCode(),
                        PlanStatus.COMPLETED.getCode())
                .count();
        if (locked > 0) {
            throw new BusinessException("存在 待审核/已发布/已完成 的计划，无法删除");
        }
        this.removeByIds(ids);
        itemMapper.delete(new LambdaUpdateWrapper<CoursePlanItem>().in(CoursePlanItem::getPlanId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        CoursePlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Integer cur = plan.getStatus();
        if (cur != PlanStatus.DRAFT.getCode() && cur != PlanStatus.REJECTED.getCode()) {
            throw new BusinessException("仅 草稿 / 驳回 状态可提交审核，当前：" + PlanStatus.labelOf(cur));
        }
        CoursePlan entity = new CoursePlan();
        entity.setId(id);
        entity.setStatus(PlanStatus.PENDING.getCode());
        entity.setApproverId(null);
        entity.setApproverName(null);
        entity.setApproveRemark(null);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approverId, String approverName, String remark) {
        CoursePlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != PlanStatus.PENDING.getCode()) {
            throw new BusinessException("仅 待审核 状态可通过，当前：" + PlanStatus.labelOf(plan.getStatus()));
        }
        CoursePlan entity = new CoursePlan();
        entity.setId(id);
        entity.setStatus(PlanStatus.PUBLISHED.getCode());
        entity.setApproverId(approverId);
        entity.setApproverName(approverName);
        entity.setApproveRemark(remark);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, Long approverId, String approverName, String remark) {
        CoursePlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != PlanStatus.PENDING.getCode()) {
            throw new BusinessException("仅 待审核 状态可驳回，当前：" + PlanStatus.labelOf(plan.getStatus()));
        }
        if (StringUtils.isBlank(remark)) {
            throw new BusinessException("驳回意见不能为空");
        }
        CoursePlan entity = new CoursePlan();
        entity.setId(id);
        entity.setStatus(PlanStatus.REJECTED.getCode());
        entity.setApproverId(approverId);
        entity.setApproverName(approverName);
        entity.setApproveRemark(remark);
        this.updateById(entity);
    }

    @Override
    public List<CoursePlan> listByTeacher(Long teacherId) {
        return this.lambdaQuery()
                .eq(teacherId != null, CoursePlan::getCreateBy, teacherId)
                .orderByDesc(CoursePlan::getCreateTime)
                .list();
    }
}