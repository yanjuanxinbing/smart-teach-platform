package com.smartteach.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 根据用户ID查询其拥有的角色编码集合 */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 根据用户ID查询其拥有的权限标识集合（菜单/按钮 permission 字段） */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
