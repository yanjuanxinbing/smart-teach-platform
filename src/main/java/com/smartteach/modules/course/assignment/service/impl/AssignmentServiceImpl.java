package com.smartteach.modules.course.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.assignment.dto.AssignmentQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSaveDTO;
import com.smartteach.modules.course.assignment.entity.Assignment;
import com.smartteach.modules.course.assignment.entity.AssignmentSubmission;
import com.smartteach.modules.course.assignment.mapper.AssignmentMapper;
import com.smartteach.modules.course.assignment.mapper.AssignmentSubmissionMapper;
import com.smartteach.modules.course.assignment.service.AssignmentService;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.mapper.CourseChapterMapper;
import com.smartteach.modules.system.entity.SysAssignmentClass;
import com.smartteach.modules.system.entity.SysUserClass;
import com.smartteach.modules.system.mapper.SysAssignmentClassMapper;
import com.smartteach.modules.system.mapper.SysUserClassMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {

    private final AssignmentSubmissionMapper submissionMapper;
    private final SysAssignmentClassMapper assignmentClassMapper;
    private final SysUserClassMapper userClassMapper;
    private final CourseChapterMapper chapterMapper;

    public AssignmentServiceImpl(AssignmentSubmissionMapper submissionMapper,
                                 SysAssignmentClassMapper assignmentClassMapper,
                                 SysUserClassMapper userClassMapper,
                                 CourseChapterMapper chapterMapper) {
        this.submissionMapper = submissionMapper;
        this.assignmentClassMapper = assignmentClassMapper;
        this.userClassMapper = userClassMapper;
        this.chapterMapper = chapterMapper;
    }

    @Override
    public PageResult<Assignment> page(AssignmentQueryDTO query) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCourseId() != null, Assignment::getCourseId, query.getCourseId())
                .eq(query.getChapterId() != null, Assignment::getChapterId, query.getChapterId())
                .eq(query.getContentId() != null, Assignment::getContentId, query.getContentId())
                .like(StringUtils.hasText(query.getKeyword()), Assignment::getTitle, query.getKeyword())
                .eq(query.getStatus() != null, Assignment::getStatus, query.getStatus())
                .ne(Boolean.TRUE.equals(query.getExcludeDraft()), Assignment::getStatus, 0)
                .orderByDesc(Assignment::getCreateTime);

        // 教师端按目标班级筛选：用 apply 传参，避免 inSql 拼接 SQL 注入风险
        if (query.getClassId() != null) {
            wrapper.apply("id IN (SELECT assignment_id FROM assignment_target_class WHERE class_id = {0} AND deleted = 0)", query.getClassId());
        }

        IPage<Assignment> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        enrich(page.getRecords());
        return PageResult.of(page);
    }

    @Override
    public PageResult<Assignment> myPage(AssignmentQueryDTO query) {
        Long studentId = UserContext.getUserId();
        if (studentId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 拿到这个学生所在的班级
        List<Long> myClassIds = userClassMapper.selectList(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getUserId, studentId))
                .stream().map(SysUserClass::getClassId).collect(Collectors.toList());
        if (myClassIds.isEmpty()) {
            return PageResult.of(new Page<>(query.getPageNum(), query.getPageSize()));
        }

        // 2. 拿到这些班级被哪些作业指向
        List<Long> myAssignmentIds = assignmentClassMapper.selectList(
                new LambdaQueryWrapper<SysAssignmentClass>().in(SysAssignmentClass::getClassId, myClassIds))
                .stream().map(SysAssignmentClass::getAssignmentId).distinct().collect(Collectors.toList());
        if (myAssignmentIds.isEmpty()) {
            return PageResult.of(new Page<>(query.getPageNum(), query.getPageSize()));
        }

        // 3. 组合查询：必须落在我的班级里、不是草稿、按关键字/状态再过滤
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Assignment::getId, myAssignmentIds)
                .ne(Assignment::getStatus, 0)
                .like(StringUtils.hasText(query.getKeyword()), Assignment::getTitle, query.getKeyword())
                .eq(query.getStatus() != null, Assignment::getStatus, query.getStatus())
                .orderByDesc(Assignment::getCreateTime);
        IPage<Assignment> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        enrich(page.getRecords());
        return PageResult.of(page);
    }

    @Override
    public Assignment detail(Long id) {
        Assignment a = this.getById(id);
        if (a == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 顺手把 classIds 填上，给前端编辑表单回填
        List<Long> classIds = assignmentClassMapper.selectList(
                new LambdaQueryWrapper<SysAssignmentClass>().eq(SysAssignmentClass::getAssignmentId, id))
                .stream().map(SysAssignmentClass::getClassId).collect(Collectors.toList());
        a.setClassIds(classIds);
        return a;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(AssignmentSaveDTO dto) {
        Assignment a = new Assignment();
        BeanUtils.copyProperties(dto, a);
        if (a.getTotalScore() == null) {
            a.setTotalScore(new BigDecimal("100"));
        }
        if (a.getStatus() == null) {
            a.setStatus(0);
        }
        this.save(a);
        // 写目标班级关联
        if (dto.getClassIds() != null && !dto.getClassIds().isEmpty() && a.getId() != null) {
            batchInsertAssignmentClass(a.getId(), dto.getClassIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 按截止时间和当前时间自动决定状态：
        //   新截止时间 > now  → 已发布（1）
        //   新截止时间 ≤ now  → 已截止（2）
        a.setStatus(deriveStatusByDeadline(a.getDeadline()));
        this.updateById(a);

        // 目标班级：先软删旧的，再插新的（整批替换）
        assignmentClassMapper.delete(
                new LambdaQueryWrapper<SysAssignmentClass>().eq(SysAssignmentClass::getAssignmentId, dto.getId()));
        if (dto.getClassIds() != null && !dto.getClassIds().isEmpty()) {
            batchInsertAssignmentClass(dto.getId(), dto.getClassIds());
        }
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
        // 同步软删目标班级关联
        for (Long id : ids) {
            assignmentClassMapper.delete(
                    new LambdaQueryWrapper<SysAssignmentClass>().eq(SysAssignmentClass::getAssignmentId, id));
        }
    }

    /**
     * 自动关闭已过期作业
     */
    public int autoCloseExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> expired = this.lambdaQuery()
                .eq(Assignment::getStatus, 1)
                .isNotNull(Assignment::getDeadline)
                .le(Assignment::getDeadline, now)
                .list();
        if (expired.isEmpty()) {
            return 0;
        }
        List<Long> ids = expired.stream().map(Assignment::getId).collect(Collectors.toList());
        Assignment update = new Assignment();
        update.setStatus(2);
        this.update(update, new LambdaQueryWrapper<Assignment>().in(Assignment::getId, ids));
        return ids.size();
    }

    // ---- helpers ----

    /**
     * 列表结果批量回填展示字段：目标班级ID列表 + 所属章节标题。
     * 一次性按 assignmentId / chapterId 批量查询，避免 N+1。
     */
    private void enrich(List<Assignment> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> assignmentIds = records.stream()
                .map(Assignment::getId).filter(Objects::nonNull).collect(Collectors.toList());

        // 目标班级：一次查出全部关联，按 assignmentId 分组
        Map<Long, List<Long>> classIdMap = assignmentIds.isEmpty() ? Collections.emptyMap()
                : assignmentClassMapper.selectList(new LambdaQueryWrapper<SysAssignmentClass>()
                        .in(SysAssignmentClass::getAssignmentId, assignmentIds))
                    .stream()
                    .collect(Collectors.groupingBy(SysAssignmentClass::getAssignmentId,
                            Collectors.mapping(SysAssignmentClass::getClassId, Collectors.toList())));

        // 章节标题：一次查出去重后的章节
        Set<Long> chapterIds = records.stream()
                .map(Assignment::getChapterId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> chapterTitleMap = chapterIds.isEmpty() ? Collections.emptyMap()
                : chapterMapper.selectBatchIds(chapterIds).stream()
                    .collect(Collectors.toMap(CourseChapter::getId, CourseChapter::getChapterTitle));

        for (Assignment a : records) {
            a.setClassIds(classIdMap.getOrDefault(a.getId(), Collections.emptyList()));
            a.setChapterTitle(chapterTitleMap.get(a.getChapterId()));
        }
    }

    private Integer deriveStatusByDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return 0;
        }
        return deadline.isAfter(LocalDateTime.now()) ? 1 : 2;
    }

    private void batchInsertAssignmentClass(Long assignmentId, List<Long> classIds) {
        Set<Long> unique = new HashSet<>();
        List<SysAssignmentClass> rows = new ArrayList<>();
        for (Long cid : classIds) {
            if (cid != null && unique.add(cid)) {
                SysAssignmentClass ac = new SysAssignmentClass();
                ac.setAssignmentId(assignmentId);
                ac.setClassId(cid);
                rows.add(ac);
            }
        }
        if (!rows.isEmpty()) {
            for (SysAssignmentClass ac : rows) {
                assignmentClassMapper.insert(ac);
            }
        }
    }
}