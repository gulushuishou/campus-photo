package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品表
 */
@Data
@TableName("work")
public class Work {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photographerId;

    private String title;

    /** 封面图 */
    private String cover;

    /** 多图，JSON 数组字符串 */
    private String images;

    /** 风格标签，逗号分隔 */
    private String styleTags;

    private String description;

    /** 状态：PENDING / PUBLISHED / REMOVED */
    private String status;

    private Integer viewCount;

    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
