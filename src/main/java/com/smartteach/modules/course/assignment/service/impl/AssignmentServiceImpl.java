package com.smartteach.modules.course.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.assignment.dto.AssignmentQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSaveDTO;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.mapper.AssignmentMapper;
import com.smartteach.modules.course.assignment.mapper.AssignmentSubmissionMapper;
import com.smartteach.modules.course.assignment.service.AssignmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {

    private final AssignmentSubmissionMapper submissionMapper;

    public AssignmentServiceImpl(AssignmentSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Override
    public PageResult<Assignment> page(AssignmentQueryDTO query) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCourseId() != null, Assignment::getCourseId, query.getCourseId())
                .eq(query.getChapterId() != null, Assignment::getChapterId, query.getChapterId())
                .eq(query.getContentId() != null, Assignment::getContentId, query.getContentId())
                .like(StringUtils.hasText(query.getKeyword()), Assignment::getTitle, query.getKeyword())
                .eq(query.getStatus() != null, Assignment::getStatus, query.getStatus())
                .orderByDesc(Assignment::getCreateTime);
        IPage<Assignment> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Assignment detail(Long id) {
        Assignment a = this.getById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return a;
    }

    @Override
    public void save(AssignmentSaveDTO dto) {
        Assignment a = new Assignment();
        BeanUtils.copyProperties(dto, a);
        if (a.getTotalScore() == null) {
            a.setTotalScore(new BigDecimal("100"));
        }
        if (a.getAllowLate() == null) {
            a.setAllowLate(1);
        }
        if (a.getStatus() == null) {
            a.setStatus(0);
        }
        this.save(a);
    }

    @Override
    public void update(AssignmentSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("id 不能为空");
        }
        Assignment existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (existing.getStatus() != null && existing.getStatus() == 2) {
            throw new BusinessException("已截止的作业不允许编辑");
        }
        Assignment a = new Assignment();
        BeanUtils.copyProperties(dto, a);
        this.updateById(a);
    }

    @Override
    public void publish(Long id) {
        Assignment a = this.getById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (a.getStatus() == null || a.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的作业可以发布");
        }
        if (!StringUtils.hasText(a.getTitle()) || a.getDeadline() == null) {
            throw new BusinessException("作业标题与截止时间必须填写");
        }
        Assignment update = new Assignment();
        update.setId(id);
        update.setStatus(1);
        this.updateById(update);
    }

    @Override
    public void close(Long id) {
        Assignment a = this.getById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (a.getStatus() == null || a.getStatus() != 1) {
            throw new BusinessException("只有已发布的作业可以关闭");
        }
        Assignment update = new Assignment();
        update.setId(id);
        update.setStatus(2);
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        // 已发布且有人提交的作业不能直接删，避免历史数据断裂
        for (Long id : ids) {
            long submittedCount = submissionMapper.selectCount(
                    new LambdaQueryWrapper<AssignmentSubmission>()
                            .eq(AssignmentSubmission::getAssignmentId, id)
                            .ne(AssignmentSubmission::getStatus, 0));
            if (submittedCount > 0) {
                Assignment a = this.getById(id);
                throw new BusinessException(
                        "作业「" + (a == null ? id : a.getTitle()) + "」已有提交记录，不允许删除");
            }
        }
        this.removeByIds(ids);
    }
}
