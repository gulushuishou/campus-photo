package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 约拍需求表
 */
@Data
@TableName("demand")
public class Demand {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /** 风格标签，逗号分隔 */
    private String styleTags;

    private LocalDateTime shootTime;

    /** 拍摄地点 */
    private String location;

    /** 预算 */
    private BigDecimal budget;

    /** 状态：RECRUITING / DONE / CLOSED */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
