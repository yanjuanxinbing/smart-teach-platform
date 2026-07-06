package com.smartteach.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.permission.entity.SysUserRole;
import com.smartteach.modules.permission.mapper.SysUserRoleMapper;
import com.smartteach.modules.user.dto.UserQueryDTO;
import com.smartteach.modules.user.dto.UserSaveDTO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import com.smartteach.modules.user.service.SysUserService;
import com.smartteach.modules.user.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserVO> page(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.isNotBlank(query.getRealName()), SysUser::getRealName, query.getRealName())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        IPage<UserVO> voPage = page.convert(this::toVO);
        return PageResult.of(voPage);
    }

    @Override
    public UserVO getDetail(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        UserVO vo = toVO(user);
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(UserSaveDTO dto) {
        boolean exists = this.lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).exists();
        if (exists) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        String rawPassword = StringUtils.isNotBlank(dto.getPassword()) ? dto.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPassword));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        this.save(user);
        bindRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserSaveDTO dto) {
        SysUser exists = this.getById(dto.getId());
        if (exists == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        if (StringUtils.isNotBlank(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(null); // 不覆盖原密码
        }
        this.updateById(user);
        userRoleMapper.delete(new LambdaUpdateWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        bindRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        super.removeByIds(ids);
        userRoleMapper.delete(new LambdaUpdateWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void changeOwnPassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        resetPassword(userId, newPassword);
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    private void bindRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        roleIds.forEach(roleId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        });
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
