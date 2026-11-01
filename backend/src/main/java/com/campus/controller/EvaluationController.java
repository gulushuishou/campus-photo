package com.campus.controller;

import com.campus.common.Result;
import com.campus.service.EvaluationService;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 评价接口
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Data
    public static class EvaluateRequest {
        private Integer score;
        private String content;
    }

    @PostMapping("/{orderId}")
    public Result<Void> evaluate(@PathVariable Long orderId,
                                 @RequestBody EvaluateRequest request,
                                 Authentication auth) {
        evaluationService.evaluate(orderId, (Long) auth.getPrincipal(),
                request.getScore(), request.getContent());
        return Result.success();
    }
}
