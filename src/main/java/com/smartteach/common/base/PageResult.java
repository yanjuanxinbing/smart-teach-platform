package com.smartteach.common.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应结果
 */
@Data
public class PageResult<T> {

    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> list;

    public PageResult() {
    }

    public PageResult(IPage<T> page) {
        this.total = page.getTotal();
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.list = page.getRecords();
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page);
    }
}
