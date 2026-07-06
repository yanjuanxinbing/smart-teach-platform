package com.smartteach.modules.auth.service;

import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.JwtUtil;
import com.smartteach.modules.auth.dto.LoginDTO;
import com.smartteach.modules.auth.vo.LoginVO;
import com.smartteach.modules.user.entity.SysUser;
import com.smartteach.modules.user.mapper.SysUserMapper;
import com.smartteach.modules.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysUserMapper userMapper;
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
}
