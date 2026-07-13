package com.smartteach.modules.course.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSubmissionSaveDTO;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.mapper.AssignmentSubmissionMapper;
import com.smartteach.modules.course.assignment.service.AssignmentService;
import com.smartteach.modules.course.assignment.service.AssignmentSubmissionService;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssignmentSubmissionServiceImpl extends ServiceImpl<AssignmentSubmissionMapper, AssignmentSubmission>
        implements AssignmentSubmissionService {

    private static final Pattern CLASS_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5A-Za-z0-9·]+班)");

    private final AssignmentService assignmentService;
    private final SysUserService userService;

    public AssignmentSubmissionServiceImpl(AssignmentService assignmentService, SysUserService userService) {
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @Override
    public PageResult<AssignmentSubmission> page(AssignmentSubmissionQueryDTO query) {
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getAssignmentId() != null, AssignmentSubmission::getAssignmentId, query.getAssignmentId())
                .like(StringUtils.hasText(query.getStudentName()), AssignmentSubmission::getStudentName, query.getStudentName())
                .like(StringUtils.hasText(query.getClassName()), AssignmentSubmission::getClassName, query.getClassName())
                .eq(query.getStatus() != null, AssignmentSubmission::getStatus, query.getStatus())
                .eq(query.getIsLate() != null, AssignmentSubmission::getIsLate, query.getIsLate())
                .orderByDesc(AssignmentSubmission::getSubmitTime)
                .orderByDesc(AssignmentSubmission::getCreateTime);
        IPage<AssignmentSubmission> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        fillAssignmentTitle(page.getRecords());
        return PageResult.of(page);
    }

    @Override
    public PageResult<AssignmentSubmission> myPage(AssignmentSubmissionQueryDTO query) {
        // 学生视角：student_id 直接由服务端注入，不信客户端
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getStudentId, studentId)
                .eq(query.getAssignmentId() != null, AssignmentSubmission::getAssignmentId, query.getAssignmentId())
                .eq(query.getStatus() != null, AssignmentSubmission::getStatus, query.getStatus())
                .orderByDesc(AssignmentSubmission::getSubmitTime)
                .orderByDesc(AssignmentSubmission::getCreateTime);
        IPage<AssignmentSubmission> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public AssignmentSubmission latest(Long assignmentId, Long studentId) {
        if (studentId == null) {
            studentId = UserContext.getUserId();
        }
        if (studentId == null) {
            return null;
        }
        return this.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                .eq(AssignmentSubmission::getStudentId, studentId)
                .orderByDesc(AssignmentSubmission::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public Long saveDraft(AssignmentSubmissionSaveDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Assignment assignment = assignmentService.getById(dto.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        if (assignment.getStatus() != null && assignment.getStatus() == 2) {
            throw new BusinessException("作业已截止，不能再保存草稿");
        }

        // upsert: 同一 (assignmentId, studentId, status=0) 唯一一行
        AssignmentSubmission existing = this.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, dto.getAssignmentId())
                .eq(AssignmentSubmission::getStudentId, userId)
                .eq(AssignmentSubmission::getStatus, 0)
                .one();
        if (existing != null) {
            // 更新字段
            existing.setSubmitText(dto.getSubmitText());
            existing.setFileUrl(dto.getFileUrl());
            existing.setOriginalName(dto.getOriginalName());
            existing.setFileSuffix(dto.getFileSuffix());
            existing.setFileSize(dto.getFileSize());
            this.updateById(existing);
            return existing.getId();
        }

        AssignmentSubmission sub = new AssignmentSubmission();
        BeanUtils.copyProperties(dto, sub);
        sub.setStudentId(userId);
        fillStudentInfo(sub, userId);
        sub.setStatus(0);
        sub.setIsLate(0);
        this.save(sub);
        return sub.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(AssignmentSubmissionSaveDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Assignment assignment = assignmentService.getById(dto.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        if (assignment.getStatus() == null || assignment.getStatus() != 1) {
            throw new BusinessException("作业当前不接受提交");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean late = assignment.getDeadline() != null && now.isAfter(assignment.getDeadline());
        if (late) {
            throw new BusinessException("已超过截止时间，不允许迟交");
        }

        // 若前端提交时带了已有草稿的 id，则先把该草稿删掉，避免脏数据。
        // 否则直接 insert 新行。
        if (dto.getId() != null) {
            AssignmentSubmission existing = this.getById(dto.getId());
            if (existing != null && existing.getStatus() != null && existing.getStatus() == 0
                    && userId.equals(existing.getStudentId())) {
                this.removeById(dto.getId());
            }
        }

        AssignmentSubmission sub = new AssignmentSubmission();
        BeanUtils.copyProperties(dto, sub);
        sub.setId(null);
        sub.setStudentId(userId);
        fillStudentInfo(sub, userId);
        sub.setStatus(1);
        sub.setIsLate(late ? 1 : 0);
        sub.setSubmitTime(now);
        this.save(sub);
    }

    @Override
    public void grade(Long id, BigDecimal score, String comment) {
        AssignmentSubmission sub = this.getById(id);
        if (sub == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (sub.getStatus() == null || sub.getStatus() != 1) {
            throw new BusinessException("只能批改已提交状态的作业");
        }
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("成绩必须在 0-100 之间");
        }
        Long graderId = UserContext.getUserId();
        String graderName = graderId == null ? null : lookupRealName(graderId);
        AssignmentSubmission update = new AssignmentSubmission();
        update.setId(id);
        update.setScore(score);
        update.setComment(comment);
        update.setStatus(2);
        update.setGraderId(graderId);
        update.setGraderName(graderName);
        update.setGradeTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    public void remove(List<Long> ids) {
        this.removeByIds(ids);
    }

    /**
     * 填入 student_name，并把 remark 中的 "XX班" 截出来放进 class_name。
     * 找不到班级就置 null，绝不抛错。
     */
    private void fillStudentInfo(AssignmentSubmission sub, Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            return;
        }
        sub.setStudentName(user.getRealName());
        String remark = user.getRemark();
        if (StringUtils.hasText(remark)) {
            Matcher m = CLASS_PATTERN.matcher(remark);
            if (m.find()) {
                sub.setClassName(m.group(1));
            }
        }
    }

    private String lookupRealName(Long userId) {
        SysUser user = userService.getById(userId);
        return user == null ? null : user.getRealName();
    }

    /**
     * 按 assignment_id 批量补充作业标题（列表展示用），作业已删除则留空。
     */
    private void fillAssignmentTitle(List<AssignmentSubmission> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> ids = records.stream()
                .map(AssignmentSubmission::getAssignmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> titleMap = assignmentService.listByIds(ids).stream()
                .collect(Collectors.toMap(Assignment::getId, Assignment::getTitle, (a, b) -> a));
        records.forEach(r -> r.setAssignmentTitle(titleMap.get(r.getAssignmentId())));
    }
}
