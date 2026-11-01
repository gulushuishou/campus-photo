package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.entity.Demand;
import com.campus.entity.Evaluation;
import com.campus.entity.Order;
import com.campus.entity.User;
import com.campus.entity.Work;
import com.campus.mapper.DemandMapper;
import com.campus.mapper.EvaluationMapper;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import com.campus.mapper.WorkMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务（管理端 ECharts 数据）
 */
@Service
public class StatisticsService {

    private final UserMapper userMapper;
    private final WorkMapper workMapper;
    private final DemandMapper demandMapper;
    private final OrderMapper orderMapper;
    private final EvaluationMapper evaluationMapper;

    public StatisticsService(UserMapper userMapper, WorkMapper workMapper,
                             DemandMapper demandMapper, OrderMapper orderMapper,
                             EvaluationMapper evaluationMapper) {
        this.userMapper = userMapper;
        this.workMapper = workMapper;
        this.demandMapper = demandMapper;
        this.orderMapper = orderMapper;
        this.evaluationMapper = evaluationMapper;
    }

    @Data
    public static class Overview {
        private Long userCount;
        private Long photographerCount;
        private Long workCount;
        private Long demandCount;
        private Long orderCount;
        private Long evaluationCount;
    }

    /**
     * 概览统计
     */
    public Overview overview() {
        Overview o = new Overview();
        o.setUserCount(userMapper.selectCount(null));
        o.setPhotographerCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "PHOTOGRAPHER")));
        o.setWorkCount(workMapper.selectCount(null));
        o.setDemandCount(demandMapper.selectCount(null));
        o.setOrderCount(orderMapper.selectCount(null));
        o.setEvaluationCount(evaluationMapper.selectCount(null));
        return o;
    }

    @Data
    public static class TagStat {
        private String tag;
        private Long count;
    }

    /**
     * 热门风格标签统计（从作品和需求中统计）
     */
    public List<TagStat> tagStatistics() {
        Map<String, Long> tagCount = new HashMap<>();
        // 统计作品标签
        for (Work work : workMapper.selectList(null)) {
            countTags(tagCount, work.getStyleTags());
        }
        // 统计需求标签
        for (Demand demand : demandMapper.selectList(null)) {
            countTags(tagCount, demand.getStyleTags());
        }
        List<TagStat> result = new ArrayList<>();
        tagCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> {
                    TagStat s = new TagStat();
                    s.setTag(e.getKey());
                    s.setCount(e.getValue());
                    result.add(s);
                });
        return result;
    }

    @Data
    public static class OrderStatusStat {
        private String status;
        private Long count;
    }

    /**
     * 订单状态分布
     */
    public List<OrderStatusStat> orderStatusStatistics() {
        List<OrderStatusStat> result = new ArrayList<>();
        String[] statuses = {"PENDING", "CONFIRMED", "FINISHED", "EVALUATED", "CANCELLED"};
        String[] names = {"待确认", "已确认", "拍摄完成", "已评价", "已取消"};
        for (int i = 0; i < statuses.length; i++) {
            OrderStatusStat s = new OrderStatusStat();
            s.setStatus(names[i]);
            s.setCount(orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>().eq(Order::getStatus, statuses[i])));
            result.add(s);
        }
        return result;
    }

    private void countTags(Map<String, Long> map, String tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (String tag : tags.split(",")) {
            String t = tag.trim();
            if (!t.isEmpty()) {
                map.put(t, map.getOrDefault(t, 0L) + 1);
            }
        }
    }
}
