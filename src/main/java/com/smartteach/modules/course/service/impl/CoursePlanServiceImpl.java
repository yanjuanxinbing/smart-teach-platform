package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
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
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getPlanTitle()), CoursePlan::getPlanTitle, query.getPlanTitle())
                .eq(query.getCourseId() != null, CoursePlan::getCourseId, query.getCourseId())
                .eq(StringUtils.isNotBlank(query.getSemester()), CoursePlan::getSemester, query.getSemester())
                .like(StringUtils.isNotBlank(query.getClassName()), CoursePlan::getClassName, query.getClassName())
                .eq(query.getStatus() != null, CoursePlan::getStatus, query.getStatus())
                .orderByDesc(CoursePlan::getCreateTime);
        IPage<CoursePlan> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
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
    public void save(CoursePlanSaveDTO dto) {
        CoursePlan plan = new CoursePlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) {
            plan.setStatus(0);
        }
        this.save(plan);
        saveItems(plan.getId(), dto.getItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CoursePlanSaveDTO dto) {
        CoursePlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != null && plan.getStatus() == 2) {
            throw new BusinessException("已结课的计划不可编辑");
        }
        CoursePlan entity = new CoursePlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
        // 删除并重建明细
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
        this.removeByIds(ids);
        itemMapper.delete(new LambdaUpdateWrapper<CoursePlanItem>().in(CoursePlanItem::getPlanId, ids));
    }

    @Override
    public void submit(Long id) {
        CoursePlan plan = new CoursePlan();
        plan.setId(id);
        plan.setStatus(1);
        this.updateById(plan);
    }

    @Override
    public void approve(Long id, Long approverId, String approverName, String remark) {
        CoursePlan plan = new CoursePlan();
        plan.setId(id);
        plan.setStatus(2);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }

    @Override
    public void reject(Long id, Long approverId, String approverName, String remark) {
        CoursePlan plan = new CoursePlan();
        plan.setId(id);
        plan.setStatus(3);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }
}
