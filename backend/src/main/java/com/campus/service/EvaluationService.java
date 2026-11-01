package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.SensitiveWordFilter;
import com.campus.common.enums.OrderStatusEnum;
import com.campus.common.exception.BusinessException;
import com.campus.entity.Evaluation;
import com.campus.entity.Order;
import com.campus.entity.User;
import com.campus.mapper.EvaluationMapper;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评价与信用积分服务：双向评价 + 信用积分变动
 */
@Service
public class EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    public EvaluationService(EvaluationMapper evaluationMapper,
                             OrderMapper orderMapper,
                             UserMapper userMapper,
                             SensitiveWordFilter sensitiveWordFilter) {
        this.evaluationMapper = evaluationMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    /**
     * 提交评价：仅"拍摄完成"状态的订单可评价，评价后订单流转为"已评价"
     * 信用分变动：1-2 星扣分，3 星不变，4-5 星加分
     */
    @Transactional
    public void evaluate(Long orderId, Long fromUserId, Integer score, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatusEnum.FINISHED.getCode().equals(order.getStatus())) {
            throw new BusinessException("仅拍摄完成的订单可评价");
        }
        // 敏感词校验
        if (sensitiveWordFilter.contains(content)) {
            throw new BusinessException("评价内容包含敏感词，请修改后重试");
        }

        // 判断评价人身份，确定被评价人
        Long toUserId;
        if (order.getUserId().equals(fromUserId)) {
            toUserId = order.getPhotographerId(); // 用户评价摄影师
        } else if (order.getPhotographerId().equals(fromUserId)) {
            toUserId = order.getUserId(); // 摄影师评价用户
        } else {
            throw new BusinessException("无权评价该订单");
        }

        // 是否已评价过
        Long count = evaluationMapper.selectCount(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getOrderId, orderId)
                .eq(Evaluation::getFromUserId, fromUserId));
        if (count > 0) {
            throw new BusinessException("已评价过该订单");
        }

        // 信用分变动
        int delta = calcCreditDelta(score);

        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(orderId);
        evaluation.setFromUserId(fromUserId);
        evaluation.setToUserId(toUserId);
        evaluation.setScore(score);
        evaluation.setContent(content);
        evaluation.setCreditDelta(delta);
        evaluation.setCreateTime(LocalDateTime.now());
        evaluationMapper.insert(evaluation);

        // 更新被评价人信用分
        User toUser = userMapper.selectById(toUserId);
        if (toUser != null) {
            int newScore = (toUser.getCreditScore() == null ? 100 : toUser.getCreditScore()) + delta;
            toUser.setCreditScore(Math.max(0, newScore));
            toUser.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(toUser);
        }

        // 双方都评价完后订单才流转为"已评价"（这里简化为任一方评价即流转）
        order.setStatus(OrderStatusEnum.EVALUATED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private int calcCreditDelta(int score) {
        return switch (score) {
            case 1 -> -10;
            case 2 -> -5;
            case 3 -> 0;
            case 4 -> 5;
            case 5 -> 10;
            default -> 0;
        };
    }
}
