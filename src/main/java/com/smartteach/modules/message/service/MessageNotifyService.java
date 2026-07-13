package com.smartteach.modules.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.message.dto.MessagePageQueryDTO;
import com.smartteach.modules.message.entity.MessageNotify;
import com.smartteach.modules.message.mapper.MessageNotifyMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内消息 service
 */
@Service
public class MessageNotifyService extends ServiceImpl<MessageNotifyMapper, MessageNotify> {

    /** 分页（按 userId 限定，tab 限定） */
    public PageResult<MessageNotify> pageOf(Long userId, MessagePageQueryDTO q) {
        LambdaQueryWrapper<MessageNotify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageNotify::getUserId, userId)
                .eq("unread".equalsIgnoreCase(q.getTab()), MessageNotify::getReadFlag, 0);
        // type 字段直接匹配（如 system/course/private...）
        if (q.getTab() != null
                && !q.getTab().isEmpty()
                && !"all".equalsIgnoreCase(q.getTab())
                && !"unread".equalsIgnoreCase(q.getTab())) {
            wrapper.eq(MessageNotify::getType, q.getTab());
        }
        wrapper.orderByDesc(MessageNotify::getCreateTime);
        IPage<MessageNotify> page = this.page(
                new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    /** 未读数 */
    public long unreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<MessageNotify>()
                .eq(MessageNotify::getUserId, userId)
                .eq(MessageNotify::getReadFlag, 0));
    }

    /** 标记某些 id 为已读 */
    public boolean markRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return false;
        return this.update(new LambdaUpdateWrapper<MessageNotify>()
                .eq(MessageNotify::getUserId, userId)
                .in(MessageNotify::getId, ids)
                .eq(MessageNotify::getReadFlag, 0)
                .set(MessageNotify::getReadFlag, 1)
                .set(MessageNotify::getReadTime, LocalDateTime.now()));
    }

    /** 全部已读 */
    public boolean markAllRead(Long userId) {
        return this.update(new LambdaUpdateWrapper<MessageNotify>()
                .eq(MessageNotify::getUserId, userId)
                .eq(MessageNotify::getReadFlag, 0)
                .set(MessageNotify::getReadFlag, 1)
                .set(MessageNotify::getReadTime, LocalDateTime.now()));
    }
}
