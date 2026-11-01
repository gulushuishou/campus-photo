package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.SensitiveWordFilter;
import com.campus.common.exception.BusinessException;
import com.campus.dto.DemandRequest;
import com.campus.entity.Demand;
import com.campus.entity.User;
import com.campus.mapper.DemandMapper;
import com.campus.mapper.UserMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 约拍需求服务
 */
@Service
public class DemandService {

    private final DemandMapper demandMapper;
    private final UserMapper userMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    public DemandService(DemandMapper demandMapper, UserMapper userMapper,
                         SensitiveWordFilter sensitiveWordFilter) {
        this.demandMapper = demandMapper;
        this.userMapper = userMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    /**
     * 发布需求
     */
    public Demand create(Long userId, DemandRequest request) {
        // 敏感词校验
        checkSensitive(request.getTitle(), request.getContent());
        Demand demand = new Demand();
        demand.setUserId(userId);
        demand.setTitle(request.getTitle());
        demand.setContent(request.getContent());
        demand.setStyleTags(request.getStyleTags());
        demand.setShootTime(request.getShootTime());
        demand.setLocation(request.getLocation());
        demand.setBudget(request.getBudget());
        demand.setStatus("RECRUITING");
        demand.setCreateTime(LocalDateTime.now());
        demand.setUpdateTime(LocalDateTime.now());
        demandMapper.insert(demand);
        return demand;
    }

    /**
     * 分页查询需求列表（可按状态、风格标签、关键词、校区/地点筛选）
     */
    public Page<Demand> page(int current, int size, String status, String styleTag, String keyword, String location) {
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Demand::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Demand::getTitle, keyword);
        }
        if (styleTag != null && !styleTag.isEmpty()) {
            wrapper.like(Demand::getStyleTags, styleTag);
        }
        if (location != null && !location.isEmpty()) {
            wrapper.like(Demand::getLocation, location);
        }
        wrapper.orderByDesc(Demand::getCreateTime);
        return demandMapper.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 获取需求详情
     */
    public Demand getById(Long id) {
        Demand demand = demandMapper.selectById(id);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        return demand;
    }

    /**
     * 编辑需求（仅发布者可编辑）
     */
    public Demand update(Long userId, Long id, DemandRequest request) {
        Demand demand = getById(id);
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException("无权编辑该需求");
        }
        checkSensitive(request.getTitle(), request.getContent());
        demand.setTitle(request.getTitle());
        demand.setContent(request.getContent());
        demand.setStyleTags(request.getStyleTags());
        demand.setShootTime(request.getShootTime());
        demand.setLocation(request.getLocation());
        demand.setBudget(request.getBudget());
        demand.setUpdateTime(LocalDateTime.now());
        demandMapper.updateById(demand);
        return demand;
    }

    /**
     * 关闭/删除需求
     */
    public void close(Long userId, Long id) {
        Demand demand = getById(id);
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该需求");
        }
        demand.setStatus("CLOSED");
        demand.setUpdateTime(LocalDateTime.now());
        demandMapper.updateById(demand);
    }

    /**
     * 删除需求（物理删除，仅发布者）
     */
    public void delete(Long userId, Long id) {
        Demand demand = getById(id);
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该需求");
        }
        demandMapper.deleteById(id);
    }

    private void checkSensitive(String title, String content) {
        if (sensitiveWordFilter.contains(title) || sensitiveWordFilter.contains(content)) {
            throw new BusinessException("内容包含敏感词，请修改后重试");
        }
    }

    @Data
    public static class DemandVO {
        private Long id;
        private String title;
        private String content;
        private String styleTags;
        private LocalDateTime shootTime;
        private String location;
        private java.math.BigDecimal budget;
        private String status;
        private LocalDateTime createTime;
        private Long userId;
        private String nickname;
        private String campus;
        private Integer creditScore;
    }

    /**
     * 转换为带发布者信息的 VO
     */
    public DemandVO toVO(Demand demand) {
        DemandVO vo = new DemandVO();
        vo.setId(demand.getId());
        vo.setTitle(demand.getTitle());
        vo.setContent(demand.getContent());
        vo.setStyleTags(demand.getStyleTags());
        vo.setShootTime(demand.getShootTime());
        vo.setLocation(demand.getLocation());
        vo.setBudget(demand.getBudget());
        vo.setStatus(demand.getStatus());
        vo.setCreateTime(demand.getCreateTime());
        vo.setUserId(demand.getUserId());
        User user = userMapper.selectById(demand.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setCampus(user.getCampus());
            vo.setCreditScore(user.getCreditScore());
        }
        return vo;
    }
}
