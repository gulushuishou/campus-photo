package com.campus.controller;

import com.campus.common.Result;
import com.campus.service.CosineSimilarityService;
import com.campus.service.RecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推荐接口
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;
    private final CosineSimilarityService cosineSimilarityService;

    public RecommendController(RecommendService recommendService,
                               CosineSimilarityService cosineSimilarityService) {
        this.recommendService = recommendService;
        this.cosineSimilarityService = cosineSimilarityService;
    }

    /**
     * 根据风格标签推荐摄影师
     */
    @GetMapping("/photographers")
    public Result<List<RecommendService.RecommendItem>> recommend(
            @RequestParam String tags,
            @RequestParam(defaultValue = "10") int topN) {
        return Result.success(recommendService.recommend(tags, topN));
    }

    /**
     * 获取标签词典
     */
    @GetMapping("/tags")
    public Result<List<String>> tags() {
        return Result.success(cosineSimilarityService.getTagDict());
    }
}
