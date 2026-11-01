package com.campus.common.enums;

/**
 * 订单状态枚举（订单状态机）
 * 待确认 -> 已确认 -> 拍摄完成 -> 已评价
 * 待确认/已确认 状态下可取消
 */
public enum OrderStatusEnum {
    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    FINISHED("FINISHED", "拍摄完成"),
    EVALUATED("EVALUATED", "已评价"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
