package com.smartteach.modules.experiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.experiment.dto.ExperimentPlanItemDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanQueryDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanSaveDTO;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.entity.ExperimentPlanItem;
import com.smartteach.modules.experiment.mapper.ExperimentPlanItemMapper;
import com.smartteach.modules.experiment.mapper.ExperimentPlanMapper;
import com.smartteach.modules.experiment.service.ExperimentPlanService;
import com.smartteach.modules.experiment.vo.ExperimentPlanDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan> implements ExperimentPlanService {

    private final ExperimentPlanItemMapper itemMapper;

    public ExperimentPlanServiceImpl(ExperimentPlanItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public PageResult<ExperimentPlan> page(ExperimentPlanQueryDTO query) {
        LambdaQueryWrapper<ExperimentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getPlanTitle()), ExperimentPlan::getPlanTitle, query.getPlanTitle())
                .eq(query.getCourseId() != null, ExperimentPlan::getCourseId, query.getCourseId())
                .eq(StringUtils.isNotBlank(query.getSemester()), ExperimentPlan::getSemester, query.getSemester())
                .like(StringUtils.isNotBlank(query.getClassName()), ExperimentPlan::getClassName, query.getClassName())
                .eq(query.getTeacherId() != null, ExperimentPlan::getTeacherId, query.getTeacherId())
                .eq(query.getStatus() != null, ExperimentPlan::getStatus, query.getStatus())
                .orderByDesc(ExperimentPlan::getCreateTime);
        IPage<ExperimentPlan> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public ExperimentPlanDetailVO detail(Long id) {
        ExperimentPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        List<ExperimentPlanItem> items = itemMapper.selectList(new LambdaQueryWrapper<ExperimentPlanItem>()
                .eq(ExperimentPlanItem::getPlanId, id)
                .orderByAsc(ExperimentPlanItem::getExpNo));
        ExperimentPlanDetailVO vo = new ExperimentPlanDetailVO();
        vo.setPlan(plan);
        vo.setItems(items);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExperimentPlan save(ExperimentPlanSaveDTO dto) {
        ExperimentPlan plan = new ExperimentPlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) plan.setStatus(0);
        this.save(plan);
        saveItems(plan.getId(), dto.getItems());
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ExperimentPlanSaveDTO dto) {
        ExperimentPlan plan = this.getById(dto.getId());
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 0 && plan.getStatus() != 3) {
            throw new BusinessException("只有草稿或驳回状态才能编辑");
        }
        ExperimentPlan entity = new ExperimentPlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
        itemMapper.delete(new LambdaUpdateWrapper<ExperimentPlanItem>().eq(ExperimentPlanItem::getPlanId, dto.getId()));
        saveItems(dto.getId(), dto.getItems());
    }

    private void saveItems(Long planId, List<ExperimentPlanItemDTO> items) {
        if (items == null || items.isEmpty()) return;
        List<ExperimentPlanItem> list = items.stream().map(item -> {
            ExperimentPlanItem entity = new ExperimentPlanItem();
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
        itemMapper.delete(new LambdaUpdateWrapper<ExperimentPlanItem>().in(ExperimentPlanItem::getPlanId, ids));
    }

    @Override
    public void submit(Long id) {
        ExperimentPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 0) {
            throw new BusinessException("只有草稿状态才能提交");
        }
        plan.setStatus(1);
        this.updateById(plan);
    }

    @Override
    public void approve(Long id, Long approverId, String approverName, String remark) {
        ExperimentPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("只有已发布状态才能审核");
        }
        plan.setStatus(2);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }

    @Override
    public void reject(Long id, Long approverId, String approverName, String remark) {
        ExperimentPlan plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("只有已发布状态才能驳回");
        }
        plan.setStatus(3);
        plan.setApproverId(approverId);
        plan.setApproverName(approverName);
        plan.setApproveRemark(remark);
        this.updateById(plan);
    }
}
