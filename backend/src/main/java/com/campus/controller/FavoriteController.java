package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Work;
import com.campus.service.FavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏接口
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/toggle/{workId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long workId, Authentication auth) {
        boolean favorited = favoriteService.toggle((Long) auth.getPrincipal(), workId);
        return Result.success(Map.of("favorited", favorited));
    }

    /**
     * 是否已收藏
     */
    @GetMapping("/check/{workId}")
    public Result<Map<String, Object>> check(@PathVariable Long workId, Authentication auth) {
        boolean favorited = favoriteService.isFavorite((Long) auth.getPrincipal(), workId);
        return Result.success(Map.of("favorited", favorited));
    }

    /**
     * 我的收藏作品列表
     */
    @GetMapping("/my")
    public Result<List<Work>> myFavorites(Authentication auth) {
        return Result.success(favoriteService.myFavorites((Long) auth.getPrincipal()));
    }
}
