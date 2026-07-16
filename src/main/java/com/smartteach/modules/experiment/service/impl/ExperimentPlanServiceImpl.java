package com.smartteach.modules.experiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.experiment.dto.ExperimentPlanItemDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanQueryDTO;
import com.smartteach.modules.experiment.dto.ExperimentPlanSaveDTO;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.entity.ExperimentPlanItem;
import com.smartteach.modules.experiment.mapper.ExperimentPlanItemMapper;
import com.smartteach.modules.experiment.mapper.ExperimentPlanMapper;
import com.smartteach.modules.experiment.service.ExperimentAssignmentService;
import com.smartteach.modules.experiment.service.ExperimentPlanService;
import com.smartteach.modules.experiment.vo.ExperimentPlanDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan> implements ExperimentPlanService {

    private final ExperimentPlanItemMapper itemMapper;
    // 实验计划 → 实验评分 自动铺记录的桥接服务。@Lazy 避免与本服务的可能循环依赖。
    private final ExperimentAssignmentService assignmentService;

    public ExperimentPlanServiceImpl(ExperimentPlanItemMapper itemMapper,
                                    @Lazy ExperimentAssignmentService assignmentService) {
        this.itemMapper = itemMapper;
        this.assignmentService = assignmentService;
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
        // 防御:items 至少 1 条 —— 否则学生端 myExperiments 步骤 3 会因 itemsByPlan=null 静默跳过该 plan,前端 list 空 total=0
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("实验计划必须至少添加 1 条实验明细");
        }
        // 防御:每条 item 的 expName 和 classDate 必填 —— 学生端按 item 展开 VO,缺字段会被静默跳过
        for (int i = 0; i < dto.getItems().size(); i++) {
            ExperimentPlanItemDTO it = dto.getItems().get(i);
            if (it == null || !StringUtils.isNotBlank(it.getExpName()) || it.getClassDate() == null) {
                throw new BusinessException("第 " + (i + 1) + " 条实验的名称/上课日期不能为空");
            }
        }
        // 防御:item.classDate 必须落在 [startDate, endDate] 范围内 —— 与 DTO @AssertTrue 重复兜底
        validateItemDatesInPlanRange(dto);
        ExperimentPlan plan = new ExperimentPlan();
        BeanUtils.copyProperties(dto, plan);
        if (plan.getStatus() == null) plan.setStatus(0);
        this.save(plan);
        saveItems(plan.getId(), dto.getItems());
        // 自动铺评分占位:给该班所有学生铺一条 status=1(未评分) 的记录,
        // 后续教师在「实验评分」页直接录成绩即可。
        try {
            assignmentService.autoCreateByPlan(plan, UserContext.getUserId());
        } catch (Exception e) {
            // 评分占位失败不应阻塞计划保存,记录日志便于排查
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("实验计划保存后自动铺评分记录失败: planId={}, className={}",
                    plan.getId(), plan.getClassName(), e);
        }
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
        // 防御:编辑时也不允许把 items 清空,否则已分配的学生会"突然看不到实验"
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("实验计划必须至少保留 1 条实验明细,不能清空");
        }
        for (int i = 0; i < dto.getItems().size(); i++) {
            ExperimentPlanItemDTO it = dto.getItems().get(i);
            if (it == null || !StringUtils.isNotBlank(it.getExpName()) || it.getClassDate() == null) {
                throw new BusinessException("第 " + (i + 1) + " 条实验的名称/上课日期不能为空");
            }
        }
        // 防御:item.classDate 必须落在 [startDate, endDate] 范围内
        validateItemDatesInPlanRange(dto);
        ExperimentPlan entity = new ExperimentPlan();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
        itemMapper.delete(new LambdaUpdateWrapper<ExperimentPlanItem>().eq(ExperimentPlanItem::getPlanId, dto.getId()));
        saveItems(dto.getId(), dto.getItems());
        // 编辑后重新触发铺记录(幂等去重):
        //   - 同一班:已存在的跳过,无副作用
        //   - 改了 className:旧班级无影响,新班级的学生会自动出现
        ExperimentPlan after = this.getById(dto.getId());
        if (after != null) {
            try {
                assignmentService.autoCreateByPlan(after, UserContext.getUserId());
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("实验计划编辑后自动铺评分记录失败: planId={}, className={}",
                        after.getId(), after.getClassName(), e);
            }
        }
    }

    /**
     * 兜底校验:实验明细的 classDate 必须落在 [startDate, endDate] 内。
     * 与 ExperimentPlanSaveDTO#isItemDatesWithinPlanRange 重复,但放在 service
     * 是为了挡住直接 hit /experiment/plan 接口、绕过 @Valid 的非法请求。
     */
    private void validateItemDatesInPlanRange(ExperimentPlanSaveDTO dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null || dto.getItems() == null) {
            return;
        }
        for (int i = 0; i < dto.getItems().size(); i++) {
            ExperimentPlanItemDTO it = dto.getItems().get(i);
            if (it == null || it.getClassDate() == null) continue;
            if (it.getClassDate().isBefore(dto.getStartDate())
                    || it.getClassDate().isAfter(dto.getEndDate())) {
                throw new BusinessException("第 " + (i + 1) + " 条实验的上课日期必须在 ["
                        + dto.getStartDate() + ", " + dto.getEndDate() + "] 范围内");
            }
        }
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
        // 允许 0(草稿) / 2(已完成) / 3(已驳回) 三种状态回退到 1(已发布)
        // —— 解决"误点通过/驳回后无法再分配"的卡点;1(已发布)→1 是幂等 noop
        if (plan.getStatus() != 0 && plan.getStatus() != 2 && plan.getStatus() != 3) {
            throw new BusinessException("当前状态不允许提交");
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

    @Override
    public List<String> listDistinctClasses() {
        // 仅从未逻辑删除的计划里取 class_name，去重 + 过滤 null/空
        LambdaQueryWrapper<ExperimentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ExperimentPlan::getClassName)
                .ne(ExperimentPlan::getClassName, "")
                .select(ExperimentPlan::getClassName)
                .groupBy(ExperimentPlan::getClassName);
        return this.list(wrapper).stream()
                .map(ExperimentPlan::getClassName)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
