package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Complaint;
import com.campus.service.ComplaintService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 投诉接口（普通用户提交投诉）
 */
@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * 提交投诉
     */
    @PostMapping
    public Result<Complaint> submit(@RequestBody ComplaintService.ComplaintRequest request,
                                    Authentication auth) {
        return Result.success(complaintService.submit((Long) auth.getPrincipal(), request));
    }
}
