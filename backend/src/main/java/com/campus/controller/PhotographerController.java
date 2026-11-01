package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.PhotographerApplyRequest;
import com.campus.entity.Photographer;
import com.campus.service.PhotographerService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 摄影师认证接口
 */
@RestController
@RequestMapping("/api/photographer")
public class PhotographerController {

    private final PhotographerService photographerService;

    public PhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    /**
     * 提交认证申请
     */
    @PostMapping("/apply")
    public Result<Photographer> apply(@Valid @RequestBody PhotographerApplyRequest request,
                                      Authentication auth) {
        return Result.success(photographerService.apply((Long) auth.getPrincipal(), request));
    }

    /**
     * 我的认证状态
     */
    @GetMapping("/my-status")
    public Result<PhotographerService.PhotographerVO> myStatus(Authentication auth) {
        Photographer p = photographerService.getMyStatus((Long) auth.getPrincipal());
        return Result.success(p == null ? null : photographerService.toVO(p));
    }
}
