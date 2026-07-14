package com.smartteach.modules.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.course.dto.CourseTeacherAssignDTO;
import com.smartteach.modules.course.dto.CourseTeacherQueryDTO;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.entity.CourseTeacher;
import com.smartteach.modules.course.mapper.CourseMapper;
import com.smartteach.modules.course.mapper.CourseTeacherMapper;
import com.smartteach.modules.course.service.CourseTeacherService;
import com.smartteach.modules.course.vo.CourseTeacherVO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import com.smartteach.modules.user.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    private final CourseMapper courseMapper;
    private final SysUserMapper userMapper;

    public CourseTeacherServiceImpl(CourseMapper courseMapper, SysUserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<CourseTeacherVO> page(CourseTeacherQueryDTO query) {
        // 1. 先分页查 course_teacher 主表
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        if (query.getCourseId() != null) wrapper.eq(CourseTeacher::getCourseId, query.getCourseId());
        if (query.getTeacherId() != null) wrapper.eq(CourseTeacher::getTeacherId, query.getTeacherId());
        if (query.getStatus() != null) wrapper.eq(CourseTeacher::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getRole())) wrapper.eq(CourseTeacher::getRole, query.getRole());
        wrapper.orderByAsc(CourseTeacher::getSort).orderByDesc(CourseTeacher::getCreateTime);

        IPage<CourseTeacher> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<CourseTeacher> records = page.getRecords();

        if (records.isEmpty()) {
            return PageResult.of(page.convert(c -> new CourseTeacherVO()));
        }

        // 2. 批量取课程、教师名称（避免 N+1）
        Set<Long> courseIds = records.stream().map(CourseTeacher::getCourseId).collect(Collectors.toSet());
        Set<Long> teacherIds = records.stream().map(CourseTeacher::getTeacherId).collect(Collectors.toSet());

        Map<Long, String> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseMapper.selectBatchIds(courseIds).forEach(c -> courseMap.put(c.getId(), c.getCourseName()));
        }

        Map<Long, String> teacherMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            // 用户表 @TableLogic 软删：自动过滤 deleted=1
            userMapper.selectBatchIds(teacherIds).forEach(u -> teacherMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }

        // 3. 关键字过滤（前端 keyword 可能匹配课程名或教师名）—— 若设置了 keyword，
        //    上面拉取的课程/教师表里筛一遍 course_name / teacher_name
        String kw = query.getKeyword();
        List<CourseTeacherVO> filtered = new ArrayList<>(records.size());
        for (CourseTeacher ct : records) {
            String courseName = courseMap.get(ct.getCourseId());
            String teacherName = teacherMap.get(ct.getTeacherId());
            if (StringUtils.isNotBlank(kw)) {
                if ((courseName == null || !courseName.contains(kw))
                        && (teacherName == null || !teacherName.contains(kw))) {
                    continue;
                }
            }
            CourseTeacherVO vo = new CourseTeacherVO();
            vo.setId(ct.getId());
            vo.setCourseId(ct.getCourseId());
            vo.setCourseName(courseName);
            vo.setTeacherId(ct.getTeacherId());
            vo.setTeacherName(teacherName);
            vo.setRole(ct.getRole());
            vo.setSort(ct.getSort());
            vo.setStatus(ct.getStatus());
            vo.setCreateTime(ct.getCreateTime());
            filtered.add(vo);
        }

        PageResult<CourseTeacherVO> result = new PageResult<>();
        result.setList(filtered);
        result.setTotal(page.getTotal());
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(CourseTeacherAssignDTO dto) {
        // 校验课程存在
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || course.getDeleted() != null && course.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 校验教师都存在
        Set<Long> teacherIds = dto.getTeachers().stream()
                .map(CourseTeacherAssignDTO.TeacherItem::getTeacherId)
                .collect(Collectors.toSet());
        if (teacherIds.isEmpty()) {
            throw new BusinessException("教师不能为空");
        }
        long existCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getId, teacherIds).eq(SysUser::getDeleted, 0));
        if (existCount != teacherIds.size()) {
            throw new BusinessException("存在无效的教师ID");
        }

        // 同一门课内，去重 + 去同教师重复行
        Set<Long> seen = new HashSet<>();
        List<CourseTeacherAssignDTO.TeacherItem> uniqueItems = new ArrayList<>();
        for (CourseTeacherAssignDTO.TeacherItem item : dto.getTeachers()) {
            if (item.getTeacherId() == null || seen.contains(item.getTeacherId())) continue;
            seen.add(item.getTeacherId());
            if (StringUtils.isBlank(item.getRole())) item.setRole("主讲");
            if (item.getSort() == null) item.setSort(0);
            uniqueItems.add(item);
        }
        if (uniqueItems.isEmpty()) {
            throw new BusinessException("教师不能为空");
        }

        // 全量替换：先软删该课程所有授课关系
        this.remove(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, dto.getCourseId()));

        // 再插新关系
        for (CourseTeacherAssignDTO.TeacherItem item : uniqueItems) {
            CourseTeacher ct = new CourseTeacher();
            ct.setCourseId(dto.getCourseId());
            ct.setTeacherId(item.getTeacherId());
            ct.setRole(item.getRole());
            ct.setSort(item.getSort());
            ct.setStatus(1);
            this.save(ct);
        }
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        CourseTeacher ct = this.getById(id);
        if (ct == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        CourseTeacher update = new CourseTeacher();
        update.setId(id);
        update.setStatus(status);
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        // 软删：继承 @TableLogic，removeByIds 写 deleted=1
        this.removeByIds(ids);
    }

    @Override
    public List<UserVO> listTeachersByCourseId(Long courseId) {
        if (courseId == null) return new ArrayList<>();
        // 先拿 course_teacher 行
        List<CourseTeacher> rows = this.lambdaQuery()
                .eq(CourseTeacher::getCourseId, courseId)
                .eq(CourseTeacher::getStatus, 1)
                .orderByAsc(CourseTeacher::getSort)
                .list();
        if (rows.isEmpty()) return new ArrayList<>();

        Set<Long> teacherIds = rows.stream().map(CourseTeacher::getTeacherId).collect(Collectors.toSet());
        List<SysUser> users = userMapper.selectBatchIds(teacherIds);

        // 拼 UserVO（复用 UserVO）
        Map<Long, CourseTeacher> roleMap = rows.stream()
                .collect(Collectors.toMap(CourseTeacher::getTeacherId, r -> r, (a, b) -> a));
        List<UserVO> result = new ArrayList<>();
        for (SysUser u : users) {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setAvatar(u.getAvatar());
            vo.setPhone(u.getPhone());
            vo.setEmail(u.getEmail());
            vo.setStatus(u.getStatus());
            CourseTeacher ct = roleMap.get(u.getId());
            if (ct != null) {
                vo.setRemark(ct.getRole());  // 暂借 remark 字段透出 role 到前端
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<CourseTeacher> listByTeacherId(Long teacherId) {
        if (teacherId == null) return new ArrayList<>();
        return this.lambdaQuery()
                .eq(CourseTeacher::getTeacherId, teacherId)
                .eq(CourseTeacher::getStatus, 1)
                .list();
    }
}