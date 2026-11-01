package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Order;
import com.campus.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Order> create(@RequestBody Order order, Authentication auth) {
        order.setUserId(getUserId(auth));
        return Result.success(orderService.createOrder(order));
    }

    /**
     * 摄影师确认订单
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, Authentication auth) {
        orderService.confirmOrder(id, getUserId(auth));
        return Result.success();
    }

    /**
     * 摄影师标记拍摄完成
     */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id, Authentication auth) {
        orderService.finishOrder(id, getUserId(auth));
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        orderService.cancelOrder(id, getUserId(auth));
        return Result.success();
    }

    /**
     * 我的订单列表
     */
    @GetMapping("/my")
    public Result<List<Order>> myOrders(Authentication auth) {
        return Result.success(orderService.listByUser(getUserId(auth)));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
