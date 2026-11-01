package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信表
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话 id，格式：较小用户id_较大用户id */
    private String conversationId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /** 是否已读：0 未读 1 已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
