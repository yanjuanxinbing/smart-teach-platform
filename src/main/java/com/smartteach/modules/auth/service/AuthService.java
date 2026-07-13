package com.smartteach.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.JwtUtil;
import com.smartteach.modules.auth.dto.LoginDTO;
import com.smartteach.modules.auth.dto.RegisterDTO;
import com.smartteach.modules.auth.vo.LoginVO;
import com.smartteach.modules.permission.entity.SysRole;
import com.smartteach.modules.permission.mapper.SysRoleMapper;
import com.smartteach.modules.user.dto.UserSaveDTO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import com.smartteach.modules.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginVO login(LoginDTO dto) {
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(userMapper.selectRoleCodesByUserId(user.getId()));
        vo.setPermissions(userMapper.selectPermissionsByUserId(user.getId()));
        return vo;
    }

    /**
     * 自助注册（教师/学生）。
     * 流程：写库 → 复用 login() 自动发 token。前端拿到 LoginVO 后 router.push('/') 即可。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        // 1. 两次密码一致性
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        // 2. roleCode → roleId（二次校验 role 表真实存在）
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()).last("LIMIT 1"));
        if (role == null || role.getId() == null) {
            throw new BusinessException("无效的角色");
        }
        // 3. 拼 UserSaveDTO，复用 SysUserService.save() 的查重 + BCrypt + 绑角色逻辑
        UserSaveDTO save = new UserSaveDTO();
        save.setUsername(dto.getUsername());
        save.setPassword(dto.getPassword());
        save.setRealName(dto.getRealName());
        save.setPhone(dto.getPhone());
        save.setEmail(dto.getEmail());
        save.setStatus(1);                                          // 直接启用
        save.setRoleIds(Collections.singletonList(role.getId()));
        userService.save(save);                                      // 内置查重 + BCrypt

        // 4. 自动登录，复用现有 token 生成 / 权限装配逻辑
        LoginDTO login = new LoginDTO();
        login.setUsername(dto.getUsername());
        login.setPassword(dto.getPassword());
        return login(login);
    }
}
