package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.enums.OrderStatusEnum;
import com.campus.common.exception.BusinessException;
import com.campus.entity.Order;
import com.campus.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务：订单状态机流转 + 并发控制
 *
 * 状态机：待确认(PENDING) -> 已确认(CONFIRMED) -> 拍摄完成(FINISHED) -> 已评价(EVALUATED)
 *         待确认/已确认 -> 已取消(CANCELLED)
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 创建订单（待确认状态）
     * 并发控制：检查同一摄影师同一时间段是否已有"已确认"订单，防止超约
     */
    @Transactional
    public Order createOrder(Order order) {
        // 校验是否冲突（已有确认订单在同一时间段）
        Long conflictCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getPhotographerId, order.getPhotographerId())
                        .eq(Order::getShootTime, order.getShootTime())
                        .eq(Order::getStatus, OrderStatusEnum.CONFIRMED.getCode()));
        if (conflictCount > 0) {
            throw new BusinessException("该摄影师该时间段已有确认订单，请更换时间");
        }

        order.setOrderNo(generateOrderNo());
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setVersion(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    /**
     * 摄影师确认订单：待确认 -> 已确认
     * 使用乐观锁保证并发下的数据一致性
     */
    @Transactional
    public void confirmOrder(Long orderId, Long photographerId) {
        Order order = getById(orderId);
        checkOwner(order, photographerId);
        checkStatus(order, OrderStatusEnum.PENDING, "仅待确认订单可确认");
        // 再次检查时间冲突
        Long conflictCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getPhotographerId, photographerId)
                        .eq(Order::getShootTime, order.getShootTime())
                        .eq(Order::getStatus, OrderStatusEnum.CONFIRMED.getCode())
                        .ne(Order::getId, orderId));
        if (conflictCount > 0) {
            throw new BusinessException("该时间段已有其他确认订单，无法确认");
        }
        order.setStatus(OrderStatusEnum.CONFIRMED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException("订单已被其他操作修改，请刷新后重试");
        }
    }

    /**
     * 摄影师标记拍摄完成：已确认 -> 拍摄完成
     */
    @Transactional
    public void finishOrder(Long orderId, Long photographerId) {
        Order order = getById(orderId);
        checkOwner(order, photographerId);
        checkStatus(order, OrderStatusEnum.CONFIRMED, "仅已确认订单可标记完成");
        order.setStatus(OrderStatusEnum.FINISHED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 取消订单：待确认/已确认 -> 已取消
     */
    @Transactional
    public void cancelOrder(Long orderId, Long operatorId) {
        Order order = getById(orderId);
        if (!order.getUserId().equals(operatorId) && !order.getPhotographerId().equals(operatorId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())
                && !OrderStatusEnum.CONFIRMED.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 标记已评价：拍摄完成 -> 已评价（由评价服务调用）
     */
    @Transactional
    public void markEvaluated(Long orderId) {
        Order order = getById(orderId);
        order.setStatus(OrderStatusEnum.EVALUATED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    public Order getById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    public List<Order> listByUser(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .and(w -> w.eq(Order::getUserId, userId).or().eq(Order::getPhotographerId, userId))
                .orderByDesc(Order::getCreateTime));
    }

    private void checkOwner(Order order, Long photographerId) {
        if (!order.getPhotographerId().equals(photographerId)) {
            throw new BusinessException("无权操作该订单");
        }
    }

    private void checkStatus(Order order, OrderStatusEnum expected, String msg) {
        if (!expected.getCode().equals(order.getStatus())) {
            throw new BusinessException(msg);
        }
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
