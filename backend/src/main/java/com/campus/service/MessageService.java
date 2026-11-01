package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.SensitiveWordFilter;
import com.campus.common.exception.BusinessException;
import com.campus.entity.Message;
import com.campus.entity.User;
import com.campus.mapper.MessageMapper;
import com.campus.mapper.UserMapper;
import lombok.Data;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 私信服务：消息落库 + 实时推送 + 未读计数 + 会话列表
 */
@Service
public class MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SensitiveWordFilter sensitiveWordFilter;

    public MessageService(MessageMapper messageMapper, UserMapper userMapper,
                          SimpMessagingTemplate messagingTemplate,
                          SensitiveWordFilter sensitiveWordFilter) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.messagingTemplate = messagingTemplate;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    @Data
    public static class SendRequest {
        private Long toUserId;
        private String content;
    }

    @Data
    public static class ConversationItem {
        private Long otherUserId;
        private String nickname;
        private String avatar;
        private String lastMessage;
        private LocalDateTime lastTime;
        private Long unreadCount;
    }

    /**
     * 发送私信：落库后通过 WebSocket 实时推送给接收方
     */
    public Message send(Long fromUserId, SendRequest request) {
        if (sensitiveWordFilter.contains(request.getContent())) {
            throw new BusinessException("消息内容包含敏感词，无法发送");
        }
        Message message = new Message();
        message.setConversationId(buildConversationId(fromUserId, request.getToUserId()));
        message.setFromUserId(fromUserId);
        message.setToUserId(request.getToUserId());
        message.setContent(request.getContent());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 实时推送：推送到接收方的专属 topic /topic/user/{toUserId}
        messagingTemplate.convertAndSend("/topic/user/" + request.getToUserId(), message);
        return message;
    }

    /**
     * 获取会话历史消息（并标记对方发来的消息为已读）
     */
    public List<Message> getConversation(Long userId, Long otherUserId) {
        String conversationId = buildConversationId(userId, otherUserId);
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreateTime));
        // 标记对方发来的消息为已读
        boolean needUpdate = false;
        for (Message msg : list) {
            if (msg.getToUserId().equals(userId) && msg.getIsRead() == 0) {
                msg.setIsRead(1);
                messageMapper.updateById(msg);
                needUpdate = true;
            }
        }
        return list;
    }

    /**
     * 获取会话列表：所有与我相关的会话，含对方信息、最后一条消息、未读数
     */
    public List<ConversationItem> listConversations(Long userId) {
        // 查询所有与我相关的消息，按时间倒序
        List<Message> allMessages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId))
                .orderByDesc(Message::getCreateTime));

        // 按对方用户分组，取每组最新一条
        List<ConversationItem> result = new ArrayList<>();
        List<Long> seenUsers = new ArrayList<>();
        for (Message msg : allMessages) {
            Long otherUserId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            if (seenUsers.contains(otherUserId)) {
                continue;
            }
            seenUsers.add(otherUserId);

            User otherUser = userMapper.selectById(otherUserId);
            ConversationItem item = new ConversationItem();
            item.setOtherUserId(otherUserId);
            item.setNickname(otherUser != null ? otherUser.getNickname() : "用户" + otherUserId);
            item.setAvatar(otherUser != null ? otherUser.getAvatar() : null);
            item.setLastMessage(msg.getContent());
            item.setLastTime(msg.getCreateTime());
            // 统计该会话中对方发来且未读的消息数
            Long unread = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getConversationId, buildConversationId(userId, otherUserId))
                    .eq(Message::getFromUserId, otherUserId)
                    .eq(Message::getIsRead, 0));
            item.setUnreadCount(unread);
            result.add(item);
        }
        return result;
    }

    /**
     * 未读消息数
     */
    public Long unreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    /**
     * 会话 id：较小用户id_较大用户id，保证同一会话唯一
     */
    private String buildConversationId(Long a, Long b) {
        return a < b ? a + "_" + b : b + "_" + a;
    }
}
