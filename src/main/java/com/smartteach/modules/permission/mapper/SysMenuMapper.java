package com.smartteach.modules.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.permission.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据角色ID集合查询其对应的菜单ID集合
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
