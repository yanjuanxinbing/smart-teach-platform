package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.system.dto.SysDeptSaveDTO;
import com.smartteach.modules.system.entity.SysDept;
import com.smartteach.modules.system.mapper.SysDeptMapper;
import com.smartteach.modules.system.service.SysDeptService;
import com.smartteach.modules.system.vo.SysDeptOptionVO;
import com.smartteach.modules.system.vo.SysDeptTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDeptTreeVO> tree() {
        List<SysDept> all = this.lambdaQuery().orderByAsc(SysDept::getSort).list();
        return buildTree(all, 0L);
    }

    @Override
    public List<SysDeptOptionVO> treeOptions() {
        // 只加载启用且未删除的部门，丢弃 leader/phone/email 等 PII
        List<SysDept> all = this.lambdaQuery()
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSort)
                .list();
        return buildOptionTree(all, 0L);
    }

    @Override
    public void save(SysDeptSaveDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        if (dept.getParentId() == null) dept.setParentId(0L);
        if (dept.getStatus() == null) dept.setStatus(1);
        this.save(dept);
    }

    @Override
    public void update(SysDeptSaveDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        this.updateById(dept);
    }

    @Override
    public void remove(Long id) {
        boolean hasChildren = this.lambdaQuery().eq(SysDept::getParentId, id).exists();
        if (hasChildren) {
            throw new BusinessException("请先删除子部门");
        }
        this.removeById(id);
    }

    private List<SysDeptTreeVO> buildTree(List<SysDept> all, Long parentId) {
        List<SysDeptTreeVO> result = new ArrayList<>();
        for (SysDept d : all) {
            if (d.getParentId().equals(parentId)) {
                SysDeptTreeVO vo = new SysDeptTreeVO();
                BeanUtils.copyProperties(d, vo);
                vo.setChildren(buildTree(all, d.getId()));
                result.add(vo);
            }
        }
        result.sort(Comparator.comparing(o -> o.getSort() == null ? 0 : o.getSort()));
        return result;
    }

    private List<SysDeptOptionVO> buildOptionTree(List<SysDept> all, Long parentId) {
        List<SysDeptOptionVO> result = new ArrayList<>();
        for (SysDept d : all) {
            if (d.getParentId().equals(parentId)) {
                SysDeptOptionVO vo = new SysDeptOptionVO();
                vo.setId(d.getId());
                vo.setParentId(d.getParentId());
                vo.setDeptName(d.getDeptName());
                vo.setChildren(buildOptionTree(all, d.getId()));
                result.add(vo);
            }
        }
        // 入参 all 已在 treeOptions() 入口 orderByAsc(SysDept::getSort) 排好序，
        // 递归遍历天然有序，无需再排序（且 OptionVO 没有 sort 字段）
        return result;
    }
}
