package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约订单表
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    private Long demandId;

    /** 摄影师用户 id */
    private Long photographerId;

    /** 下单用户 id */
    private Long userId;

    /** 状态：PENDING / CONFIRMED / FINISHED / EVALUATED / CANCELLED */
    private String status;

    private BigDecimal price;

    private LocalDateTime shootTime;

    private String location;

    private String remark;

    /** 乐观锁版本号 */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
