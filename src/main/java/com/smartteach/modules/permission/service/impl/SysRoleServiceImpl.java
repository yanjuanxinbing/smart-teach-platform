package com.smartteach.modules.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.permission.entity.SysRole;
import com.smartteach.modules.permission.entity.SysRoleMenu;
import com.smartteach.modules.permission.mapper.SysRoleMapper;
import com.smartteach.modules.permission.mapper.SysRoleMenuMapper;
import com.smartteach.modules.permission.service.SysRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;

    public SysRoleServiceImpl(SysRoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public PageResult<SysRole> page(PageQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(SysRole::getRoleName, query.getKeyword())
                    .or().like(SysRole::getRoleCode, query.getKeyword());
        }
        wrapper.orderByAsc(SysRole::getSort);
        IPage<SysRole> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(SysRole role, List<Long> menuIds) {
        boolean exists = this.lambdaQuery().eq(SysRole::getRoleCode, role.getRoleCode()).exists();
        if (exists) {
            throw new BusinessException("角色编码已存在");
        }
        this.save(role);
        bindMenus(role.getId(), menuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role, List<Long> menuIds) {
        this.updateById(role);
        roleMenuMapper.delete(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, role.getId()));
        bindMenus(role.getId(), menuIds);
    }

    private void bindMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<SysRoleMenu> list = menuIds.stream().map(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            return rm;
        }).collect(Collectors.toList());
        list.forEach(roleMenuMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        super.removeByIds(ids);
        roleMenuMapper.delete(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, ids));
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    public List<SysRole> listAllEnabled() {
        return this.lambdaQuery().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSort).list();
    }
}