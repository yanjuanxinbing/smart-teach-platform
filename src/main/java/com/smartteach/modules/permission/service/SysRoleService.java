package com.smartteach.modules.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.base.PageQuery;
import com.smartteach.modules.permission.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    PageResult<SysRole> page(PageQuery query);

    void saveRole(SysRole role, List<Long> menuIds);

    void updateRole(SysRole role, List<Long> menuIds);

    void removeByIds(List<Long> ids);

    List<Long> getMenuIdsByRoleId(Long roleId);

    List<SysRole> listAllEnabled();
}
