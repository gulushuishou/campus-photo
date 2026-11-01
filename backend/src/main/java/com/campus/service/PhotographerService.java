package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.enums.RoleEnum;
import com.campus.common.exception.BusinessException;
import com.campus.dto.PhotographerApplyRequest;
import com.campus.entity.Photographer;
import com.campus.entity.User;
import com.campus.mapper.PhotographerMapper;
import com.campus.mapper.UserMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 摄影师认证服务
 */
@Service
public class PhotographerService {

    private final PhotographerMapper photographerMapper;
    private final UserMapper userMapper;

    public PhotographerService(PhotographerMapper photographerMapper, UserMapper userMapper) {
        this.photographerMapper = photographerMapper;
        this.userMapper = userMapper;
    }

    /**
     * 提交摄影师认证申请
     */
    public Photographer apply(Long userId, PhotographerApplyRequest request) {
        // 是否已申请
        Photographer existing = photographerMapper.selectOne(
                new LambdaQueryWrapper<Photographer>().eq(Photographer::getUserId, userId));
        if (existing != null) {
            if ("APPROVED".equals(existing.getAuditStatus())) {
                throw new BusinessException("你已是认证摄影师");
            }
            if ("PENDING".equals(existing.getAuditStatus())) {
                throw new BusinessException("认证申请审核中，请耐心等待");
            }
            // 被拒绝的可以重新申请
            existing.setIntro(request.getIntro());
            existing.setStyleTags(request.getStyleTags());
            existing.setAuditStatus("PENDING");
            existing.setUpdateTime(LocalDateTime.now());
            photographerMapper.updateById(existing);
            return existing;
        }

        Photographer photographer = new Photographer();
        photographer.setUserId(userId);
        photographer.setIntro(request.getIntro());
        photographer.setStyleTags(request.getStyleTags());
        photographer.setAuditStatus("PENDING");
        photographer.setWorksCount(0);
        photographer.setCreateTime(LocalDateTime.now());
        photographer.setUpdateTime(LocalDateTime.now());
        photographerMapper.insert(photographer);
        return photographer;
    }

    /**
     * 审核认证申请（管理员）
     */
    public void audit(Long photographerId, String auditStatus) {
        Photographer photographer = photographerMapper.selectById(photographerId);
        if (photographer == null) {
            throw new BusinessException("认证申请不存在");
        }
        photographer.setAuditStatus(auditStatus);
        photographer.setUpdateTime(LocalDateTime.now());
        photographerMapper.updateById(photographer);

        // 审核通过后，同步更新用户角色为 PHOTOGRAPHER
        if ("APPROVED".equals(auditStatus)) {
            User user = userMapper.selectById(photographer.getUserId());
            if (user != null) {
                user.setRole(RoleEnum.PHOTOGRAPHER.getCode());
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }
    }

    /**
     * 查询当前用户的认证状态
     */
    public Photographer getMyStatus(Long userId) {
        return photographerMapper.selectOne(
                new LambdaQueryWrapper<Photographer>().eq(Photographer::getUserId, userId));
    }

    /**
     * 认证申请列表（管理员，可按状态筛选）
     */
    public List<Photographer> list(String auditStatus) {
        LambdaQueryWrapper<Photographer> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(Photographer::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Photographer::getCreateTime);
        return photographerMapper.selectList(wrapper);
    }

    @Data
    public static class PhotographerVO {
        private Long id;
        private Long userId;
        private String auditStatus;
        private String intro;
        private String styleTags;
        private Integer worksCount;
        private String nickname;
        private String campus;
        private Integer creditScore;
    }

    public PhotographerVO toVO(Photographer photographer) {
        PhotographerVO vo = new PhotographerVO();
        vo.setId(photographer.getId());
        vo.setUserId(photographer.getUserId());
        vo.setAuditStatus(photographer.getAuditStatus());
        vo.setIntro(photographer.getIntro());
        vo.setStyleTags(photographer.getStyleTags());
        vo.setWorksCount(photographer.getWorksCount());
        User user = userMapper.selectById(photographer.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setCampus(user.getCampus());
            vo.setCreditScore(user.getCreditScore());
        }
        return vo;
    }
}
