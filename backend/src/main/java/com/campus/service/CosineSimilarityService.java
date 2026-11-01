package com.campus.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余弦相似度推荐算法
 *
 * 核心思路：将"风格标签"映射为固定维度的特征向量，
 * 计算需求向量与摄影师（作品）向量之间的余弦相似度，实现智能匹配推荐。
 *
 * 标签词典：日系、古风、校园、韩系、清新、复古、街拍、夜景、情侣、毕业照
 */
@Component
public class CosineSimilarityService {

    /** 全局标签词典（固定维度） */
    private static final List<String> TAG_DICT = List.of(
            "日系", "古风", "校园", "韩系", "清新",
            "复古", "街拍", "夜景", "情侣", "毕业照");

    /**
     * 将逗号分隔的标签字符串转换为特征向量
     */
    public double[] tagsToVector(String tags) {
        double[] vector = new double[TAG_DICT.size()];
        if (tags == null || tags.isEmpty()) {
            return vector;
        }
        Map<String, Integer> indexMap = buildIndexMap();
        for (String tag : tags.split(",")) {
            String t = tag.trim();
            if (indexMap.containsKey(t)) {
                vector[indexMap.get(t)] = 1.0;
            }
        }
        return vector;
    }

    /**
     * 计算两个向量的余弦相似度，范围 [0, 1]
     */
    public double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("向量维度不一致");
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private Map<String, Integer> buildIndexMap() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < TAG_DICT.size(); i++) {
            map.put(TAG_DICT.get(i), i);
        }
        return map;
    }

    /**
     * 获取标签词典（供前端展示）
     */
    public List<String> getTagDict() {
        return TAG_DICT;
    }

    /**
     * 示例：演示向量计算过程（供测试与论文阐述）
     */
    public String demoVector(String tags) {
        return Arrays.toString(tagsToVector(tags));
    }
}
