package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 摄影师扩展表
 */
@Data
@TableName("photographer")
public class Photographer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 审核状态：PENDING / APPROVED / REJECTED */
    private String auditStatus;

    /** 个人简介 */
    private String intro;

    /** 风格标签，逗号分隔：日系,古风,校园 */
    private String styleTags;

    /** 作品数量 */
    private Integer worksCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
