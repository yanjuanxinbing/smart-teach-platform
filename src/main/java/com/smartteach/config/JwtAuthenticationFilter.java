package com.smartteach.config;

import com.alibaba.fastjson2.JSON;
import com.smartteach.common.result.Result;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.JwtUtil;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.user.mapper.SysUserMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 每个请求解析 Authorization 头中的 JWT，校验通过后写入 SecurityContext 与 UserContext。
 * 白名单路径（登录、接口文档、静态文件等）直接放行。
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] WHITE_LIST = {
            "/auth/login", "/auth/register", "/auth/captcha",
            "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs/**",
            "/files/**", "/portal/site/**"
    };

    public JwtAuthenticationFilter(JwtUtil jwtUtil, SysUserMapper sysUserMapper) {
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI().replaceFirst(request.getContextPath(), "");
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(header);
        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            writeUnauthorized(response, "缺少登录凭证");
            return;
        }
        String token = authHeader.substring(tokenPrefix.length());
        try {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            UserContext.setUserId(userId);
            UserContext.setUsername(username);

            List<String> permissions = sysUserMapper.selectPermissionsByUserId(userId);
            List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);

            // 角色编码（如 ROLE_ADMIN）也注入 authorities，便于后续用 hasRole('ROLE_ADMIN') 简化判断
            List<SimpleGrantedAuthority> authorities = Stream.concat(
                    roles.stream(),
                    permissions.stream()
            ).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("token 解析失败: {}", e.getMessage());
            writeUnauthorized(response, "登录凭证无效");
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED.getCode(), msg);
        response.getWriter().write(JSON.toJSONString(result));
    }
}