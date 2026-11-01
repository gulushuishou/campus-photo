package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布/编辑约拍需求请求
 */
@Data
public class DemandRequest {

    @NotBlank(message = "需求标题不能为空")
    private String title;

    private String content;

    private String styleTags;

    private LocalDateTime shootTime;

    private String location;

    private BigDecimal budget;
}
