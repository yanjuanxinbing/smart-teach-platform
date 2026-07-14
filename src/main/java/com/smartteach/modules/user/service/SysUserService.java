package com.smartteach.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.user.dto.UserQueryDTO;
import com.smartteach.modules.user.dto.UserSaveDTO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.vo.UserVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    PageResult<UserVO> page(UserQueryDTO query);

    UserVO getDetail(Long id);

    void save(UserSaveDTO dto);

    void update(UserSaveDTO dto);

    void removeByIds(java.util.List<Long> ids);

    void resetPassword(Long id, String newPassword);

    void updateStatus(Long id, Integer status);

    void changeOwnPassword(Long userId, String oldPassword, String newPassword);

    SysUser getByUsername(String username);

    /**
     * 按角色编码列出所有启用的用户（任意登录可读，给授课管理"教师选择器"用）。
     */
    List<UserVO> listByRole(String roleCode);
}