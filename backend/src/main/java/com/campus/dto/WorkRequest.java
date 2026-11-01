package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 发布/编辑作品请求
 */
@Data
public class WorkRequest {

    @NotBlank(message = "作品标题不能为空")
    private String title;

    private String cover;

    /** 多图 URL 列表 */
    private List<String> images;

    private String styleTags;

    private String description;
}
