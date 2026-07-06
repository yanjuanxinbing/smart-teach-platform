package com.smartteach.modules.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.modules.permission.entity.SysMenu;
import com.smartteach.modules.permission.entity.SysUserRole;
import com.smartteach.modules.permission.mapper.SysMenuMapper;
import com.smartteach.modules.permission.mapper.SysUserRoleMapper;
import com.smartteach.modules.permission.service.SysMenuService;
import com.smartteach.modules.permission.vo.MenuTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysUserRoleMapper userRoleMapper;

    public SysMenuServiceImpl(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<MenuTreeVO> tree() {
        List<SysMenu> all = this.lambdaQuery().orderByAsc(SysMenu::getSort).list();
        return buildTree(all, 0L);
    }

    @Override
    public List<MenuTreeVO> treeByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = this.baseMapper.selectMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> menus = this.lambdaQuery()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 1)
                .ne(SysMenu::getMenuType, 3) // 树形导航只需要目录+菜单，按钮权限单独下发
                .orderByAsc(SysMenu::getSort)
                .list();
        return buildTree(menus, 0L);
    }

    @Override
    public void removeMenu(Long id) {
        boolean hasChildren = this.lambdaQuery().eq(SysMenu::getParentId, id).exists();
        if (hasChildren) {
            throw new BusinessException("请先删除子菜单");
        }
        this.removeById(id);
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> all, Long parentId) {
        List<MenuTreeVO> result = new ArrayList<>();
        for (SysMenu menu : all) {
            if (menu.getParentId().equals(parentId)) {
                MenuTreeVO vo = new MenuTreeVO();
                BeanUtils.copyProperties(menu, vo);
                vo.setChildren(buildTree(all, menu.getId()));
                result.add(vo);
            }
        }
        return result;
    }
}