package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 评价人用户 id */
    private Long fromUserId;

    /** 被评价人用户 id */
    private Long toUserId;

    /** 评分 1-5 */
    private Integer score;

    private String content;

    /** 信用分变动值 */
    private Integer creditDelta;

    private LocalDateTime createTime;
}
