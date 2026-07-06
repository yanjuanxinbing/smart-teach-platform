package com.smartteach.modules.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.modules.permission.entity.SysMenu;
import com.smartteach.modules.permission.vo.MenuTreeVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /** 全部菜单树（权限管理-菜单维护页使用） */
    List<MenuTreeVO> tree();

    /** 根据用户ID查询其可见的菜单树（左侧导航渲染使用） */
    List<MenuTreeVO> treeByUserId(Long userId);

    void removeMenu(Long id);
}
