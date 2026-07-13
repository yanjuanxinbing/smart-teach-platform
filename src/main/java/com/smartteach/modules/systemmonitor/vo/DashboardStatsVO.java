package com.smartteach.modules.systemmonitor.vo;

import lombok.Data;

/**
 * 首页统计数据：今日登录数、课程总数、资源总数
 */
@Data
public class DashboardStatsVO {

    /** 今日成功登录次数 */
    private long todayLoginCount;

    /** 课程总数 */
    private long courseCount;

    /** 资源总数（资源管理：资源列表） */
    private long resourceCount;
}