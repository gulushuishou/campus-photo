package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Complaint;
import com.campus.entity.Demand;
import com.campus.entity.Order;
import com.campus.entity.Photographer;
import com.campus.entity.User;
import com.campus.entity.Work;
import com.campus.service.ComplaintService;
import com.campus.service.PhotographerService;
import com.campus.service.StatisticsService;
import com.campus.service.WorkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.mapper.DemandMapper;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import com.campus.mapper.WorkMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端接口（仅 ADMIN 角色，已在 SecurityConfig 中限制 /api/admin/** 需要 ROLE_ADMIN）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PhotographerService photographerService;
    private final StatisticsService statisticsService;
    private final WorkService workService;
    private final UserMapper userMapper;
    private final WorkMapper workMapper;
    private final DemandMapper demandMapper;
    private final OrderMapper orderMapper;
    private final ComplaintService complaintService;

    public AdminController(PhotographerService photographerService,
                           StatisticsService statisticsService,
                           WorkService workService,
                           UserMapper userMapper,
                           WorkMapper workMapper,
                           DemandMapper demandMapper,
                           OrderMapper orderMapper,
                           ComplaintService complaintService) {
        this.photographerService = photographerService;
        this.statisticsService = statisticsService;
        this.workService = workService;
        this.userMapper = userMapper;
        this.workMapper = workMapper;
        this.demandMapper = demandMapper;
        this.orderMapper = orderMapper;
        this.complaintService = complaintService;
    }

    /**
     * 摄影师认证申请列表
     */
    @GetMapping("/photographer/applications")
    public Result<List<PhotographerService.PhotographerVO>> applications(
            @RequestParam(required = false) String auditStatus) {
        List<PhotographerService.PhotographerVO> vos = photographerService.list(auditStatus)
                .stream().map(photographerService::toVO).toList();
        return Result.success(vos);
    }

    /**
     * 审核摄影师认证（APPROVED / REJECTED）
     */
    @PostMapping("/photographer/{id}/audit")
    public Result<Void> auditPhotographer(@PathVariable Long id, @RequestParam String auditStatus) {
        photographerService.audit(id, auditStatus);
        return Result.success();
    }

    /**
     * 用户列表（管理）
     */
    @GetMapping("/users")
    public Result<List<User>> users() {
        List<User> users = userMapper.selectList(null);
        // 不返回密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    /**
     * 封禁/解封用户
     */
    @PostMapping("/user/{id}/status")
    public Result<Void> changeUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 作品列表（管理，含未审核/已下架）
     */
    @GetMapping("/works")
    public Result<List<Work>> allWorks() {
        return Result.success(workMapper.selectList(null));
    }

    /**
     * 下架/恢复作品
     */
    @PostMapping("/work/{id}/status")
    public Result<Void> changeWorkStatus(@PathVariable Long id, @RequestParam String status) {
        Work work = workMapper.selectById(id);
        if (work != null) {
            work.setStatus(status);
            workMapper.updateById(work);
        }
        return Result.success();
    }

    /**
     * 概览统计
     */
    @GetMapping("/statistics/overview")
    public Result<StatisticsService.Overview> overview() {
        return Result.success(statisticsService.overview());
    }

    /**
     * 热门标签统计
     */
    @GetMapping("/statistics/tags")
    public Result<List<StatisticsService.TagStat>> tags() {
        return Result.success(statisticsService.tagStatistics());
    }

    /**
     * 订单状态分布
     */
    @GetMapping("/statistics/order-status")
    public Result<List<StatisticsService.OrderStatusStat>> orderStatus() {
        return Result.success(statisticsService.orderStatusStatistics());
    }

    /**
     * 需求列表（管理，含所有状态）
     */
    @GetMapping("/demands")
    public Result<List<Demand>> allDemands() {
        return Result.success(demandMapper.selectList(null));
    }

    /**
     * 下架/恢复需求
     */
    @PostMapping("/demand/{id}/status")
    public Result<Void> changeDemandStatus(@PathVariable Long id, @RequestParam String status) {
        Demand demand = demandMapper.selectById(id);
        if (demand != null) {
            demand.setStatus(status);
            demandMapper.updateById(demand);
        }
        return Result.success();
    }

    /**
     * 全部订单列表（管理）
     */
    @GetMapping("/orders")
    public Result<List<Order>> allOrders() {
        return Result.success(orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)));
    }

    /**
     * 投诉列表（管理）
     */
    @GetMapping("/complaints")
    public Result<List<Complaint>> complaints(@RequestParam(required = false) String status) {
        return Result.success(complaintService.list(status));
    }

    /**
     * 处理投诉（仲裁）
     */
    @PostMapping("/complaint/{id}/resolve")
    public Result<Void> resolveComplaint(@PathVariable Long id, @RequestParam String result) {
        complaintService.resolve(id, result);
        return Result.success();
    }
}
