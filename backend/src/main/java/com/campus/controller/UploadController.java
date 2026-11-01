package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 上传单个文件
     */
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        String url = saveFile(file);
        return Result.success(url);
    }

    /**
     * 上传多个文件
     */
    @PostMapping("/multiple")
    public Result<List<String>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(saveFile(file));
            }
        }
        return Result.success(urls);
    }

    /**
     * 保存文件到本地，返回可访问的 URL
     */
    private String saveFile(MultipartFile file) {
        // 获取原始文件名后缀
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 使用绝对路径，确保与静态资源映射一致
        File baseDir = new File(uploadPath).getAbsoluteFile();
        File dir = new File(baseDir, datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }

        // 返回可访问的 URL
        return "/upload/" + datePath + "/" + fileName;
    }
}
