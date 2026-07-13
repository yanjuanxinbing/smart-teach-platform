package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.system.dto.SysClassBatchAddDTO;
import com.smartteach.modules.system.dto.SysClassMemberDTO;
import com.smartteach.modules.system.dto.SysClassQueryDTO;
import com.smartteach.modules.system.dto.SysClassSaveDTO;
import com.smartteach.modules.system.entity.SysAssignmentClass;
import com.smartteach.modules.system.entity.SysClass;
import com.smartteach.modules.system.entity.SysDept;
import com.smartteach.modules.system.entity.SysUserClass;
import com.smartteach.modules.system.mapper.SysAssignmentClassMapper;
import com.smartteach.modules.system.mapper.SysClassMapper;
import com.smartteach.modules.system.mapper.SysUserClassMapper;
import com.smartteach.modules.system.service.SysClassService;
import com.smartteach.modules.system.service.SysDeptService;
import com.smartteach.modules.system.vo.SysClassBatchAddResultVO;
import com.smartteach.modules.system.vo.SysClassVO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.service.SysUserService;
import com.smartteach.modules.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {

    private final SysUserClassMapper userClassMapper;
    private final SysAssignmentClassMapper assignmentClassMapper;
    private final SysDeptService deptService;
    private final SysUserService userService;

    public SysClassServiceImpl(SysUserClassMapper userClassMapper,
                               SysAssignmentClassMapper assignmentClassMapper,
                               SysDeptService deptService,
                               SysUserService userService) {
        this.userClassMapper = userClassMapper;
        this.assignmentClassMapper = assignmentClassMapper;
        this.deptService = deptService;
        this.userService = userService;
    }

    @Override
    public PageResult<SysClassVO> page(SysClassQueryDTO query) {
        LambdaQueryWrapper<SysClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getDeptId() != null, SysClass::getDeptId, query.getDeptId())
                .like(StringUtils.hasText(query.getKeyword()), SysClass::getClassName, query.getKeyword())
                .orderByAsc(SysClass::getSort)
                .orderByDesc(SysClass::getCreateTime);
        IPage<SysClass> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 把这一页的 classId 收集起来，一次性 join 部门名 + 成员数，避免 N+1
        List<SysClass> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(page.convert(c -> toVO(c, null, null)));
        }
        Set<Long> deptIds = records.stream().map(SysClass::getDeptId).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = deptService.listByIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        Set<Long> classIds = records.stream().map(SysClass::getId).collect(Collectors.toSet());
        Map<Long, Integer> memberCountMap = countMembersByClassIds(classIds);
        return PageResult.of(page.convert(c -> toVO(c, deptNameMap.get(c.getDeptId()), memberCountMap.getOrDefault(c.getId(), 0))));
    }

    @Override
    public SysClassVO detail(Long id) {
        SysClass c = this.getById(id);
        if (c == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        String deptName = null;
        if (c.getDeptId() != null) {
            SysDept dept = deptService.getById(c.getDeptId());
            if (dept != null) deptName = dept.getDeptName();
        }
        int memberCount = countMembersByClassIds(Collections.singleton(id)).getOrDefault(id, 0);
        return toVO(c, deptName, memberCount);
    }

    @Override
    public void save(SysClassSaveDTO dto) {
        SysClass c = new SysClass();
        BeanUtils.copyProperties(dto, c);
        if (c.getStatus() == null) c.setStatus(1);
        if (c.getSort() == null) c.setSort(0);
        this.save(c);
    }

    @Override
    public void update(SysClassSaveDTO dto) {
        if (dto.getId() == null) throw new BusinessException("id 不能为空");
        SysClass exists = this.getById(dto.getId());
        if (exists == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        SysClass c = new SysClass();
        BeanUtils.copyProperties(dto, c);
        this.updateById(c);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        for (Long id : ids) {
            long memberCount = userClassMapper.selectCount(
                    new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getClassId, id));
            if (memberCount > 0) {
                SysClass c = this.getById(id);
                throw new BusinessException(
                        "班级「" + (c == null ? id : c.getClassName()) + "」仍有成员，无法删除");
            }
            // 关联 assignment_target_class 行软删除，让历史作业仍可显示班级名（前端用 classMap 渲染）
            assignmentClassMapper.delete(
                    new LambdaQueryWrapper<SysAssignmentClass>().eq(SysAssignmentClass::getClassId, id));
        }
        this.removeByIds(ids);
    }

    @Override
    public List<SysClassVO> listByDept(Long deptId) {
        if (deptId == null) return Collections.emptyList();
        List<SysClass> list = this.lambdaQuery()
                .eq(SysClass::getDeptId, deptId)
                .eq(SysClass::getStatus, 1)
                .orderByAsc(SysClass::getSort)
                .list();
        SysDept dept = deptService.getById(deptId);
        String deptName = dept == null ? null : dept.getDeptName();
        return list.stream().map(c -> toVO(c, deptName, null)).collect(Collectors.toList());
    }

    @Override
    public List<SysClassVO> listAllEnabled() {
        List<SysClass> list = this.lambdaQuery()
                .eq(SysClass::getStatus, 1)
                .orderByAsc(SysClass::getSort)
                .list();
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> deptIds = list.stream().map(SysClass::getDeptId).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = deptService.listByIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        return list.stream()
                .map(c -> toVO(c, deptNameMap.get(c.getDeptId()), null))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysClassVO> listByCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Collections.emptyList();
        List<Long> classIds = userClassMapper.selectList(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getUserId, userId))
                .stream().map(SysUserClass::getClassId).collect(Collectors.toList());
        if (classIds.isEmpty()) return Collections.emptyList();
        List<SysClass> list = this.listByIds(classIds);
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> deptIds = list.stream().map(SysClass::getDeptId).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = deptService.listByIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        return list.stream()
                .map(c -> toVO(c, deptNameMap.get(c.getDeptId()), null))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVO> listMembers(Long classId, String roleName) {
        List<Long> userIds = userClassMapper.selectList(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getClassId, classId))
                .stream().map(SysUserClass::getUserId).collect(Collectors.toList());
        if (userIds.isEmpty()) return Collections.emptyList();
        List<UserVO> result = new ArrayList<>();
        for (Long uid : userIds) {
            UserVO vo = userService.getDetail(uid);
            if (vo != null) result.add(vo);
        }
        // 入参是中文角色名（"教师"/"学生"），与 UserVO.roleNames 直接匹配
        if (!StringUtils.hasText(roleName)) {
            return result;
        }
        return result.stream()
                .filter(u -> u.getRoleNames() != null && u.getRoleNames().stream().anyMatch(r -> r.equals(roleName)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMembers(SysClassMemberDTO dto) {
        if (dto.getClassId() == null) throw new BusinessException("班级ID不能为空");
        if (!this.lambdaQuery().eq(SysClass::getId, dto.getClassId()).exists()) {
            throw new BusinessException("班级不存在");
        }
        // 全量替换：先把班级所有现有成员软删除
        userClassMapper.delete(
                new LambdaQueryWrapper<SysUserClass>().eq(SysUserClass::getClassId, dto.getClassId()));
        // 写入新成员
        if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            return;
        }
        // 去重 + 过滤 null
        Set<Long> unique = new HashSet<>();
        for (Long uid : dto.getUserIds()) {
            if (uid != null) unique.add(uid);
        }
        for (Long uid : unique) {
            SysUserClass uc = new SysUserClass();
            uc.setUserId(uid);
            uc.setClassId(dto.getClassId());
            userClassMapper.insert(uc);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysClassBatchAddResultVO batchAddMembers(SysClassBatchAddDTO dto) {
        SysClassBatchAddResultVO result = new SysClassBatchAddResultVO();
        int total = 0, ok = 0, skip = 0, fail = 0;
        List<SysClassBatchAddResultVO.FailItem> errors = new ArrayList<>();
        if (dto == null || dto.getItems() == null) {
            result.setTotal(0); result.setSuccess(0); result.setSkipped(0); result.setFailed(0);
            result.setErrors(errors);
            return result;
        }

        // 用 Map 把这一批里出现的班级名 / 用户名批量查一次
        Set<String> classNames = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        for (SysClassBatchAddDTO.Item it : dto.getItems()) {
            if (it == null) continue;
            if (StringUtils.hasText(it.getClassName())) classNames.add(it.getClassName().trim());
            if (StringUtils.hasText(it.getUsername())) usernames.add(it.getUsername().trim());
        }

        // 班级名 → classId；只查 status=1 已启用的
        Map<String, Long> classMap = Collections.emptyMap();
        if (!classNames.isEmpty()) {
            classMap = this.lambdaQuery()
                    .in(SysClass::getClassName, classNames)
                    .eq(SysClass::getStatus, 1)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(SysClass::getClassName, SysClass::getId, (a, b) -> a));
        }

        // 用户名 → userId（注意：这里复用了 BaseEntity 的 @TableLogic，
        // 自动加 deleted=0 过滤，软删除过的用户不再被匹配——这是预期的行为）
        Map<String, Long> userMap = Collections.emptyMap();
        if (!usernames.isEmpty()) {
            userMap = userService.lambdaQuery()
                    .in(SysUser::getUsername, usernames)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(SysUser::getUsername, SysUser::getId, (a, b) -> a));
        }

        for (SysClassBatchAddDTO.Item it : dto.getItems()) {
            if (it == null) continue;
            total++;
            String cn = it.getClassName() == null ? "" : it.getClassName().trim();
            String un = it.getUsername() == null ? "" : it.getUsername().trim();
            Long classId = classMap.get(cn);
            Long userId = userMap.get(un);
            if (classId == null) {
                fail++;
                errors.add(failItem(cn, un, "班级不存在或未启用"));
                continue;
            }
            if (userId == null) {
                fail++;
                errors.add(failItem(cn, un, "账号不存在"));
                continue;
            }
            // 已存在则跳过（idempotent）
            Long existing = userClassMapper.selectCount(
                    new LambdaQueryWrapper<SysUserClass>()
                            .eq(SysUserClass::getClassId, classId)
                            .eq(SysUserClass::getUserId, userId));
            if (existing != null && existing > 0) {
                skip++;
                continue;
            }
            SysUserClass uc = new SysUserClass();
            uc.setClassId(classId);
            uc.setUserId(userId);
            userClassMapper.insert(uc);
            ok++;
        }
        result.setTotal(total);
        result.setSuccess(ok);
        result.setSkipped(skip);
        result.setFailed(fail);
        result.setErrors(errors);
        return result;
    }

    private SysClassBatchAddResultVO.FailItem failItem(String cn, String un, String reason) {
        SysClassBatchAddResultVO.FailItem fi = new SysClassBatchAddResultVO.FailItem();
        fi.setClassName(cn);
        fi.setUsername(un);
        fi.setReason(reason);
        return fi;
    }

    // ----------- private helpers -----------

    /**
     * 一次性 GROUP BY 查一批班级的成员数，避免 page 列表的 N+1
     */
    private Map<Long, Integer> countMembersByClassIds(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) return Collections.emptyMap();
        QueryWrapper<SysUserClass> qw = new QueryWrapper<>();
        qw.select("class_id AS classId, COUNT(*) AS cnt")
                .in("class_id", classIds)
                .eq("deleted", 0)
                .groupBy("class_id");
        List<Map<String, Object>> rows = userClassMapper.selectMaps(qw);
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object cid = row.get("classId");
            Object cnt = row.get("cnt");
            if (cid != null && cnt != null) {
                result.put(((Number) cid).longValue(), ((Number) cnt).intValue());
            }
        }
        return result;
    }

    private SysClassVO toVO(SysClass c, String deptName, Integer memberCount) {
        SysClassVO vo = new SysClassVO();
        BeanUtils.copyProperties(c, vo);
        vo.setDeptName(deptName);
        if (memberCount != null) vo.setMemberCount(memberCount);
        return vo;
    }
}