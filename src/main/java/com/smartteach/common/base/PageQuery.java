package com.smartteach.common.base;

import lombok.Data;

/**
 * 通用分页查询参数基类，各模块的查询DTO可继承此类
 */
@Data
public class PageQuery {

    /** 当前页码，从1开始 */
    private long pageNum = 1;

    /** 每页条数 */
    private long pageSize = 10;

    /** 关键字模糊搜索 */
    private String keyword;

    /** 排序字段 */
    private String sortField;

    /** 排序方式 asc/desc */
    private String sortOrder;
}
