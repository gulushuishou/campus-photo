package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.exception.BusinessException;
import com.campus.entity.Complaint;
import com.campus.entity.Order;
import com.campus.mapper.ComplaintMapper;
import com.campus.mapper.OrderMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉服务
 */
@Service
public class ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final OrderMapper orderMapper;

    public ComplaintService(ComplaintMapper complaintMapper, OrderMapper orderMapper) {
        this.complaintMapper = complaintMapper;
        this.orderMapper = orderMapper;
    }

    @Data
    public static class ComplaintRequest {
        private Long orderId;
        private String content;
    }

    /**
     * 提交投诉（用户对订单发起投诉）
     */
    public Complaint submit(Long userId, ComplaintRequest request) {
        if (request.getOrderId() == null) {
            throw new BusinessException("请指定订单");
        }
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId) && !order.getPhotographerId().equals(userId)) {
            throw new BusinessException("无权投诉该订单");
        }
        Complaint complaint = new Complaint();
        complaint.setUserId(userId);
        complaint.setOrderId(request.getOrderId());
        complaint.setContent(request.getContent());
        complaint.setStatus("PENDING");
        complaint.setCreateTime(LocalDateTime.now());
        complaintMapper.insert(complaint);
        return complaint;
    }

    /**
     * 投诉列表（管理员）
     */
    public List<Complaint> list(String status) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectList(wrapper);
    }

    /**
     * 处理投诉（管理员）
     */
    public void resolve(Long complaintId, String result) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException("投诉不存在");
        }
        complaint.setStatus("RESOLVED");
        complaint.setResult(result);
        complaintMapper.updateById(complaint);
    }
}
