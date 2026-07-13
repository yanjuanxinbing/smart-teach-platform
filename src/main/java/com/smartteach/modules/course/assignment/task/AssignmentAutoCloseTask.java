package com.smartteach.modules.course.assignment.task;

import com.smartteach.modules.course.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每 5 分钟扫描一次，把已发布且截止时间已过的作业自动改为已截止。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentAutoCloseTask {

    private final AssignmentService assignmentService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void autoClose() {
        try {
            int count = assignmentService.autoCloseExpired();
            if (count > 0) {
                log.info("AssignmentAutoCloseTask: 本轮关闭 {} 个过期作业", count);
            }
        } catch (Exception e) {
            log.warn("AssignmentAutoCloseTask 执行失败：{}", e.getMessage());
        }
    }
}
