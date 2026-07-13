package com.smartteach.modules.portal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用 名称-数值 对，供各类图表（柱状图/饼图/折线图/直方图）复用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVO {
    private String name;
    private Long value;
}