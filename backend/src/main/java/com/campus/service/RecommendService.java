package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.entity.Photographer;
import com.campus.entity.User;
import com.campus.entity.Work;
import com.campus.mapper.PhotographerMapper;
import com.campus.mapper.UserMapper;
import com.campus.mapper.WorkMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 推荐服务：基于余弦相似度 + 信用分权重的摄影师智能推荐
 */
@Service
public class RecommendService {

    private final PhotographerMapper photographerMapper;
    private final WorkMapper workMapper;
    private final UserMapper userMapper;
    private final CosineSimilarityService cosineSimilarityService;

    public RecommendService(PhotographerMapper photographerMapper,
                            WorkMapper workMapper,
                            UserMapper userMapper,
                            CosineSimilarityService cosineSimilarityService) {
        this.photographerMapper = photographerMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
        this.cosineSimilarityService = cosineSimilarityService;
    }

    @Data
    public static class RecommendItem {
        private Long photographerId;
        private Long userId;
        private String nickname;
        private String avatar;
        private String intro;
        private String styleTags;
        private Integer creditScore;
        private Double similarity;
        private Double score;
    }

    /**
     * 根据需求标签推荐摄影师
     * 综合得分 = 余弦相似度 * 0.7 + 信用分归一化 * 0.3
     */
    public List<RecommendItem> recommend(String demandTags, int topN) {
        double[] demandVector = cosineSimilarityService.tagsToVector(demandTags);

        List<Photographer> photographers = photographerMapper.selectList(
                new LambdaQueryWrapper<Photographer>()
                        .eq(Photographer::getAuditStatus, "APPROVED"));

        List<RecommendItem> items = new ArrayList<>();
        for (Photographer p : photographers) {
            // 计算摄影师所有已发布作品标签与需求的平均相似度
            List<Work> works = workMapper.selectList(new LambdaQueryWrapper<Work>()
                    .eq(Work::getPhotographerId, p.getId())
                    .eq(Work::getStatus, "PUBLISHED"));

            double sim = 0;
            if (!works.isEmpty()) {
                for (Work w : works) {
                    sim += cosineSimilarityService.cosineSimilarity(
                            demandVector, cosineSimilarityService.tagsToVector(w.getStyleTags()));
                }
                sim /= works.size();
            } else {
                sim = cosineSimilarityService.cosineSimilarity(
                        demandVector, cosineSimilarityService.tagsToVector(p.getStyleTags()));
            }

            User user = userMapper.selectById(p.getUserId());
            if (user == null) {
                continue;
            }
            int credit = user.getCreditScore() == null ? 100 : user.getCreditScore();
            // 信用分归一化到 [0,1]
            double creditNorm = Math.min(credit, 100) / 100.0;
            double finalScore = sim * 0.7 + creditNorm * 0.3;

            RecommendItem item = new RecommendItem();
            item.setPhotographerId(p.getId());
            item.setUserId(user.getId());
            item.setNickname(user.getNickname());
            item.setAvatar(user.getAvatar());
            item.setIntro(p.getIntro());
            item.setStyleTags(p.getStyleTags());
            item.setCreditScore(credit);
            item.setSimilarity(Math.round(sim * 10000) / 10000.0);
            item.setScore(Math.round(finalScore * 10000) / 10000.0);
            items.add(item);
        }

        items.sort(Comparator.comparingDouble(RecommendItem::getScore).reversed());
        return items.size() > topN ? items.subList(0, topN) : items;
    }
}
