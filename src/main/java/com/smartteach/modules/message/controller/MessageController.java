package com.smartteach.modules.message.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.message.dto.MarkReadDTO;
import com.smartteach.modules.message.dto.MessagePageQueryDTO;
import com.smartteach.modules.message.entity.MessageNotify;
import com.smartteach.modules.message.service.MessageNotifyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 站内消息中心
 * <p>
 * - /message/page          分页（admin 消息中心 + portal 我的消息共用）
 * - /message/unread-count  顶栏铃铛未读数
 * - /message/read          标记若干条为已读
 * - /message/read-all      全部标记为已读
 */
@Api(tags = "消息中心")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageNotifyService messageService;

    @ApiOperation("分页查询我的消息")
    @GetMapping("/page")
    public Result<PageResult<MessageNotify>> page(MessagePageQueryDTO query) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.pageOf(userId, query));
    }

    @ApiOperation("当前用户未读数（顶栏铃铛）")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        Long userId = UserContext.getUserId();
        Map<String, Long> m = new HashMap<>();
        m.put("unread", messageService.unreadCount(userId));
        return Result.success(m);
    }

    @ApiOperation("标记若干条为已读")
    @PostMapping("/read")
    public Result<Boolean> markRead(@RequestBody MarkReadDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.markRead(userId, dto.getIds()));
    }

    @ApiOperation("全部标记为已读")
    @PostMapping("/read-all")
    public Result<Boolean> markAllRead() {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.markAllRead(userId));
    }
}
