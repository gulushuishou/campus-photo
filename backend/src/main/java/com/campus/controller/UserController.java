package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.UpdateProfileRequest;
import com.campus.entity.User;
import com.campus.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取我的信息
     */
    @GetMapping("/me")
    public Result<User> me(Authentication auth) {
        return Result.success(userService.getProfile((Long) auth.getPrincipal()));
    }

    /**
     * 更新我的资料（含头像）
     */
    @PutMapping("/me")
    public Result<User> updateMe(@RequestBody UpdateProfileRequest request, Authentication auth) {
        return Result.success(userService.updateProfile((Long) auth.getPrincipal(), request));
    }

    /**
     * 查询用户公开信息
     */
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserPublicInfo(id));
    }
}
