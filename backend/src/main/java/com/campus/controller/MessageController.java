package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Message;
import com.campus.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信接口
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 发送私信
     */
    @PostMapping
    public Result<Message> send(@RequestBody MessageService.SendRequest request,
                                Authentication auth) {
        return Result.success(messageService.send((Long) auth.getPrincipal(), request));
    }

    /**
     * 获取与某人的会话记录
     */
    @GetMapping("/conversation/{otherUserId}")
    public Result<List<Message>> conversation(@PathVariable Long otherUserId,
                                              Authentication auth) {
        return Result.success(messageService.getConversation((Long) auth.getPrincipal(), otherUserId));
    }

    /**
     * 未读消息数
     */
    @GetMapping("/unread")
    public Result<Long> unread(Authentication auth) {
        return Result.success(messageService.unreadCount((Long) auth.getPrincipal()));
    }

    /**
     * 会话列表
     */
    @GetMapping("/conversations")
    public Result<List<MessageService.ConversationItem>> conversations(Authentication auth) {
        return Result.success(messageService.listConversations((Long) auth.getPrincipal()));
    }
}
