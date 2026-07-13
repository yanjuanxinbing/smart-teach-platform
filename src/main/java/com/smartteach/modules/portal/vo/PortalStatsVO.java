package com.smartteach.modules.portal.vo;

import lombok.Data;

import java.util.List;

/**
 * 门户首页/数据洞察页 图表数据聚合
 */
@Data
public class PortalStatsVO {

    /** 饼图：课程性质分布（必修/选修） */
    private List<NameValueVO> courseTypeDistribution;

    /** 柱状图：各类型教学资源数量 */
    private List<NameValueVO> resourceTypeCount;

    /** 折线图：近6个月已发布门户内容数量趋势 */
    private List<NameValueVO> monthlyArticleTrend;

    /** 直方图：课程学时区间分布 */
    private List<NameValueVO> courseHoursHistogram;
}