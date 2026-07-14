package com.smartteach.modules.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageQuery;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.permission.entity.SysMenu;
import com.smartteach.modules.permission.entity.SysRole;
import com.smartteach.modules.permission.entity.SysRoleMenu;
import com.smartteach.modules.permission.mapper.SysMenuMapper;
import com.smartteach.modules.permission.mapper.SysRoleMapper;
import com.smartteach.modules.permission.mapper.SysRoleMenuMapper;
import com.smartteach.modules.permission.service.SysRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    public SysRoleServiceImpl(SysRoleMenuMapper roleMenuMapper, SysMenuMapper menuMapper) {
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
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
        // LinkedHashSet 保序：先写管理员勾的，再补祖先
        Set<Long> ids = new LinkedHashSet<>(menuIds);
        ids.addAll(collectAncestorIds(ids));
        if (ids.isEmpty()) {
            return;
        }
        List<SysRoleMenu> list = ids.stream().map(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            return rm;
        }).collect(Collectors.toList());
        list.forEach(roleMenuMapper::insert);
    }

    /**
     * 取出菜单集合的所有祖先 menuId（含父级、祖父级…，直到 0）。
     * 用于"勾了按钮自动带上页面"——页面级权限（*.list）通常挂在菜单型菜单上，
     * 只勾按钮不勾菜单会导致用户拿不到 list 权限分页都进不去。
     */
    private Set<Long> collectAncestorIds(Set<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, Long> idToParent = menuMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysMenu::getId, SysMenu::getParentId, (a, b) -> a));
        Set<Long> ancestors = new HashSet<>();
        for (Long id : menuIds) {
            Long parent = idToParent.get(id);
            int guard = 0;
            while (parent != null && parent != 0L && !ancestors.contains(parent)) {
                ancestors.add(parent);
                parent = idToParent.get(parent);
                if (++guard > 32) {
                    // 防意外成环打破无限循环（菜单树最多 3 层，32 远大于此）
                    break;
                }
            }
        }
        return ancestors;
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