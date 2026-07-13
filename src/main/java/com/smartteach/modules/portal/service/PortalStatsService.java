package com.smartteach.modules.portal.service;

import com.smartteach.modules.portal.vo.PortalStatsVO;

public interface PortalStatsService {

    /** 门户前台公开统计数据（无需登录） */
    PortalStatsVO getStats();
}