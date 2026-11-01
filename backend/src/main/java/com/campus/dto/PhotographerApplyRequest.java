package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 摄影师认证申请请求
 */
@Data
public class PhotographerApplyRequest {

    @NotBlank(message = "个人简介不能为空")
    private String intro;

    @NotBlank(message = "风格标签不能为空")
    private String styleTags;
}
