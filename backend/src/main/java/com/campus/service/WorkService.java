package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.SensitiveWordFilter;
import com.campus.common.exception.BusinessException;
import com.campus.dto.WorkRequest;
import com.campus.entity.Photographer;
import com.campus.entity.User;
import com.campus.entity.Work;
import com.campus.mapper.PhotographerMapper;
import com.campus.mapper.UserMapper;
import com.campus.mapper.WorkMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品服务
 */
@Service
public class WorkService {

    private final WorkMapper workMapper;
    private final PhotographerMapper photographerMapper;
    private final UserMapper userMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    public WorkService(WorkMapper workMapper, PhotographerMapper photographerMapper,
                       UserMapper userMapper, SensitiveWordFilter sensitiveWordFilter) {
        this.workMapper = workMapper;
        this.photographerMapper = photographerMapper;
        this.userMapper = userMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    /**
     * 发布作品（仅认证摄影师）
     */
    public Work create(Long userId, WorkRequest request) {
        Photographer photographer = getPhotographerByUserId(userId);
        checkSensitive(request.getTitle(), request.getDescription());
        Work work = new Work();
        work.setPhotographerId(photographer.getId());
        work.setTitle(request.getTitle());
        work.setCover(request.getCover());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            work.setImages(String.join(",", request.getImages()));
        }
        work.setStyleTags(request.getStyleTags());
        work.setDescription(request.getDescription());
        work.setStatus("PUBLISHED");
        work.setViewCount(0);
        work.setLikeCount(0);
        work.setCreateTime(LocalDateTime.now());
        work.setUpdateTime(LocalDateTime.now());
        workMapper.insert(work);

        // 更新摄影师作品数
        photographer.setWorksCount(photographer.getWorksCount() == null ? 1 : photographer.getWorksCount() + 1);
        photographer.setUpdateTime(LocalDateTime.now());
        photographerMapper.updateById(photographer);
        return work;
    }

    /**
     * 作品列表（分页，默认只显示已发布）
     */
    public Page<Work> page(int current, int size, String styleTag, String keyword, String status) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Work::getStatus, status);
        } else {
            wrapper.eq(Work::getStatus, "PUBLISHED");
        }
        if (styleTag != null && !styleTag.isEmpty()) {
            wrapper.like(Work::getStyleTags, styleTag);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Work::getTitle, keyword);
        }
        wrapper.orderByDesc(Work::getCreateTime);
        return workMapper.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 作品详情（浏览量 +1）
     */
    public Work getById(Long id) {
        Work work = workMapper.selectById(id);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }
        work.setViewCount(work.getViewCount() == null ? 1 : work.getViewCount() + 1);
        workMapper.updateById(work);
        return work;
    }

    /**
     * 编辑作品（仅作者）
     */
    public Work update(Long userId, Long id, WorkRequest request) {
        Work work = getById(id);
        Photographer photographer = getPhotographerByUserId(userId);
        if (!work.getPhotographerId().equals(photographer.getId())) {
            throw new BusinessException("无权编辑该作品");
        }
        work.setTitle(request.getTitle());
        work.setCover(request.getCover());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            work.setImages(String.join(",", request.getImages()));
        }
        work.setStyleTags(request.getStyleTags());
        work.setDescription(request.getDescription());
        work.setUpdateTime(LocalDateTime.now());
        workMapper.updateById(work);
        return work;
    }

    /**
     * 删除作品
     */
    public void delete(Long userId, Long id) {
        Work work = getById(id);
        Photographer photographer = getPhotographerByUserId(userId);
        if (!work.getPhotographerId().equals(photographer.getId())) {
            throw new BusinessException("无权删除该作品");
        }
        workMapper.deleteById(id);
        photographer.setWorksCount(Math.max(0, (photographer.getWorksCount() == null ? 1 : photographer.getWorksCount()) - 1));
        photographerMapper.updateById(photographer);
    }

    /**
     * 点赞/取消点赞（简化：直接增加点赞数）
     */
    public Work like(Long id) {
        Work work = getById(id);
        work.setLikeCount((work.getLikeCount() == null ? 0 : work.getLikeCount()) + 1);
        workMapper.updateById(work);
        return work;
    }

    /**
     * 获取某摄影师的作品列表
     */
    public List<Work> listByPhotographer(Long photographerId) {
        return workMapper.selectList(new LambdaQueryWrapper<Work>()
                .eq(Work::getPhotographerId, photographerId)
                .eq(Work::getStatus, "PUBLISHED")
                .orderByDesc(Work::getCreateTime));
    }

    private Photographer getPhotographerByUserId(Long userId) {
        Photographer photographer = photographerMapper.selectOne(
                new LambdaQueryWrapper<Photographer>().eq(Photographer::getUserId, userId));
        if (photographer == null || !"APPROVED".equals(photographer.getAuditStatus())) {
            throw new BusinessException("仅认证摄影师可发布作品");
        }
        return photographer;
    }

    private void checkSensitive(String title, String description) {
        if (sensitiveWordFilter.contains(title) || sensitiveWordFilter.contains(description)) {
            throw new BusinessException("内容包含敏感词，请修改后重试");
        }
    }

    @Data
    public static class WorkVO {
        private Long id;
        private Long photographerId;
        private String title;
        private String cover;
        private List<String> images;
        private String styleTags;
        private String description;
        private String status;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createTime;
        private String photographerName;
        // 摄影师扩展信息（详情页用）
        private Long photographerUserId;
        private String photographerAvatar;
        private String photographerIntro;
        private Integer photographerCreditScore;
    }

    public WorkVO toVO(Work work) {
        WorkVO vo = new WorkVO();
        vo.setId(work.getId());
        vo.setPhotographerId(work.getPhotographerId());
        vo.setTitle(work.getTitle());
        vo.setCover(work.getCover());
        if (work.getImages() != null && !work.getImages().isEmpty()) {
            vo.setImages(List.of(work.getImages().split(",")));
        }
        vo.setStyleTags(work.getStyleTags());
        vo.setDescription(work.getDescription());
        vo.setStatus(work.getStatus());
        vo.setViewCount(work.getViewCount());
        vo.setLikeCount(work.getLikeCount());
        vo.setCreateTime(work.getCreateTime());
        Photographer p = photographerMapper.selectById(work.getPhotographerId());
        if (p != null) {
            vo.setPhotographerIntro(p.getIntro());
            User u = userMapper.selectById(p.getUserId());
            if (u != null) {
                vo.setPhotographerName(u.getNickname());
                vo.setPhotographerUserId(u.getId());
                vo.setPhotographerAvatar(u.getAvatar());
                vo.setPhotographerCreditScore(u.getCreditScore());
            }
        }
        return vo;
    }
}
