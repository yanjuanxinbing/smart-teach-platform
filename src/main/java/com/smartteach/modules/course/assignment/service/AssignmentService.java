package com.smartteach.modules.course.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.course.assignment.dto.AssignmentQueryDTO;
import com.smartteach.modules.course.assignment.dto.AssignmentSaveDTO;
import com.smartteach.modules.course.assignment.entity.Assignment;

import java.util.List;

public interface AssignmentService extends IService<Assignment> {

    PageResult<Assignment> page(AssignmentQueryDTO query);

    Assignment detail(Long id);

    void save(AssignmentSaveDTO dto);

    void update(AssignmentSaveDTO dto);

    /** 草稿→已发布 */
    void publish(Long id);

    /** 已发布→已截止 */
    void close(Long id);

    void remove(List<Long> ids);

    /**
     * 自动关闭已过期作业（定时任务调用，状态已发布 + deadline <= now → 已截止）
     *
     * @return 本轮被关闭的作业数量
     */
    int autoCloseExpired();
}
