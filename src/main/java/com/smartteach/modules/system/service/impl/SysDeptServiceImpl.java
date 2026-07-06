package com.smartteach.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.system.dto.SysDeptSaveDTO;
import com.smartteach.modules.system.entity.SysDept;
import com.smartteach.modules.system.mapper.SysDeptMapper;
import com.smartteach.modules.system.service.SysDeptService;
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
}
