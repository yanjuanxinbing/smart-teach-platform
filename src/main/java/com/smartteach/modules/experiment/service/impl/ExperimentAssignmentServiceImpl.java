package com.smartteach.modules.experiment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.experiment.entity.ExperimentAssignment;
import com.smartteach.modules.experiment.entity.ExperimentPlan;
import com.smartteach.modules.experiment.mapper.ExperimentAssignmentMapper;
import com.smartteach.modules.experiment.mapper.ExperimentPlanMapper;
import com.smartteach.modules.experiment.service.ExperimentAssignmentService;
import com.smartteach.modules.system.entity.SysClass;
import com.smartteach.modules.system.service.SysClassService;
import com.smartteach.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperimentAssignmentServiceImpl
        extends ServiceImpl<ExperimentAssignmentMapper, ExperimentAssignment>
        implements ExperimentAssignmentService {

    private final ExperimentPlanMapper planMapper;
    private final SysClassService sysClassService;

    @Override
    public PageResult<ExperimentAssignment> page(String keyword, String className, Integer status, PageQuery query) {
        LambdaQueryWrapper<ExperimentAssignment> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.eq(ExperimentAssignment::getClassName, className);
        }
        if (status != null) {
            wrapper.eq(ExperimentAssignment::getStatus, status);
        }
        // 关键字同时匹配 student_name 与 plan_title
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ExperimentAssignment::getStudentName, keyword)
                    .or().like(ExperimentAssignment::getPlanTitle, keyword));
        }
        wrapper.orderByDesc(ExperimentAssignment::getCreateTime);
        return PageResult.of(this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper));
    }

    @Override
    public int assignByClass(Long planId, String className, Long operatorId) {
        // a) 计划存在 + status=1(已发布)
        ExperimentPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException("只有已发布的实验计划才能分配");
        }
        // b) 班级存在
        SysClass cls = sysClassService.lambdaQuery()
                .eq(SysClass::getClassName, className)
                .one();
        if (cls == null) {
            throw new BusinessException("班级不存在：" + className);
        }
        return autoCreateByPlan(plan, operatorId);
    }

    /**
     * 给定一份刚保存/更新的实验计划，按它的 className 给班里每个学生铺一条 status=1 的记录（幂等）。
     * 不做计划 status 校验：实验计划保存/更新后被调用，新计划可能在草稿状态也要铺好评分占位。
     * 与 assignByClass 的差别仅在于不做 status=1 校验，复用班级查找 + 逐人建记录的逻辑。
     */
    @Override
    public int autoCreateByPlan(ExperimentPlan plan, Long operatorId) {
        if (plan == null) return 0;
        String className = plan.getClassName();
        if (className == null || className.isEmpty()) return 0;
        Long planId = plan.getId();
        if (planId == null) return 0;

        SysClass cls = sysClassService.lambdaQuery()
                .eq(SysClass::getClassName, className)
                .one();
        if (cls == null) return 0;

        // 该班学生列表
        List<UserVO> students = sysClassService.listMembers(cls.getId(), "学生");
        if (students == null || students.isEmpty()) {
            return 0;
        }
        // 逐个分配：先 selectCount 查重（uk_plan_student 唯一索引兜底）
        int added = 0;
        for (UserVO s : students) {
            Long sid = s.getId();
            if (sid == null) continue;
            long dup = this.count(new LambdaQueryWrapper<ExperimentAssignment>()
                    .eq(ExperimentAssignment::getPlanId, planId)
                    .eq(ExperimentAssignment::getStudentId, sid));
            if (dup > 0) continue;

            ExperimentAssignment a = new ExperimentAssignment();
            a.setPlanId(planId);
            a.setPlanTitle(plan.getPlanTitle());
            a.setStudentId(sid);
            a.setStudentName(s.getRealName());
            a.setClassName(className);
            a.setPhone(s.getPhone());
            a.setStatus(1);
            a.setCreateBy(operatorId);
            a.setUpdateBy(operatorId);
            this.save(a);
            added++;
        }
        return added;
    }

    @Override
    public ExperimentAssignment assignOne(Long planId, Long studentId, Long operatorId) {
        ExperimentPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException("只有已发布的实验计划才能分配");
        }
        // 查重
        long dup = this.count(new LambdaQueryWrapper<ExperimentAssignment>()
                .eq(ExperimentAssignment::getPlanId, planId)
                .eq(ExperimentAssignment::getStudentId, studentId));
        if (dup > 0) {
            throw new BusinessException("该学生已被分配此实验计划");
        }
        ExperimentAssignment a = new ExperimentAssignment();
        a.setPlanId(planId);
        a.setPlanTitle(plan.getPlanTitle());
        a.setStudentId(studentId);
        a.setStatus(1);
        a.setCreateBy(operatorId);
        a.setUpdateBy(operatorId);
        this.save(a);
        return a;
    }

    @Override
    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        this.removeByIds(ids);
    }

    @Override
    public void markCompleted(Long id, BigDecimal score, String comment) {
        ExperimentAssignment a = this.getById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (a.getStatus() != null && a.getStatus() == 3) {
            throw new BusinessException("该分配已标记完成，无需重复操作");
        }
        // 校验成绩 0-100
        if (score != null && (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("成绩必须在 0-100 之间");
        }
        ExperimentAssignment update = new ExperimentAssignment();
        update.setId(id);
        update.setStatus(3);
        update.setScore(score);
        update.setComment(comment);
        this.updateById(update);
    }
}