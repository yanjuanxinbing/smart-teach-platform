package com.smartteach.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置（新版：基于 SecurityFilterChain Bean，不再继承 WebSecurityConfigurerAdapter）
 * - 关闭 CSRF/Session，使用无状态 JWT 鉴权
 * - 登录、注册、文档、静态资源以及"前台门户公开接口"放行；其余接口需鉴权
 * - 开启 @PreAuthorize 方法级鉴权，具体权限标识见各 Controller
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(
                                // 认证（后端 admin 的登录/登出）
                                "/auth/**",
                                // 初始化（数据脚本执行入口等）
                                "/init/**",
                                // 文档
                                "/doc.html", "/webjars/**", "/swagger-resources/**",
                                "/v2/api-docs", "/v3/api-docs/**",
                                // 静态资源
                                "/files/**",
                                // 前台门户公开接口（无需登录）
                                "/portal/site/**",
                                "/portal/course/**",
                                "/portal/codex/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
