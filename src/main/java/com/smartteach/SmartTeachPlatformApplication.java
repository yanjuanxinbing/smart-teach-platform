package com.smartteach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能化在线教学支持服务平台管理中心系统 - 启动类
 *
 * 功能模块：网站门户、课程计划管理、课程实验计划管理、实训计划管理、
 *          资源管理、权限管理、用户管理、系统设置、系统监控
 */
@SpringBootApplication
@MapperScan("com.smartteach.modules.**.mapper")
@EnableScheduling
public class SmartTeachPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTeachPlatformApplication.class, args);
        System.out.println("\n" +
                " ============================================================\n" +
                "  智能化在线教学支持服务平台管理中心系统 启动成功！\n" +
                "  接口文档地址: http://localhost:8080/api/doc.html\n" +
                " ============================================================\n");
    }
}