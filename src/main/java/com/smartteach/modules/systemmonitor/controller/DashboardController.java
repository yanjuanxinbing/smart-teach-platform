package com.smartteach.modules.systemmonitor.controller;

import com.smartteach.common.result.Result;
import com.smartteach.modules.course.service.CourseService;
import com.smartteach.modules.resource.service.ResourceService;
import com.smartteach.modules.systemmonitor.service.SysLoginLogService;
import com.smartteach.modules.systemmonitor.vo.DashboardStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端首页统计：今日登录数、课程总数、资源总数
 */
@Api(tags = "首页统计")
@RestController
@RequestMapping("/monitor/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysLoginLogService loginLogService;
    private final CourseService courseService;
    private final ResourceService resourceService;

    @ApiOperation("获取首页统计数据")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTodayLoginCount(loginLogService.countTodaySuccess());
        vo.setCourseCount(courseService.count());
        vo.setResourceCount(resourceService.count());
        return Result.success(vo);
    }
}