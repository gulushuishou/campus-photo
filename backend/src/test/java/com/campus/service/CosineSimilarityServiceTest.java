package com.campus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 余弦相似度推荐算法单元测试
 */
class CosineSimilarityServiceTest {

    private final CosineSimilarityService service = new CosineSimilarityService();

    @Test
    void testSameVectorSimilarityIsOne() {
        double[] a = service.tagsToVector("日系,校园");
        double[] b = service.tagsToVector("日系,校园");
        assertEquals(1.0, service.cosineSimilarity(a, b), 0.0001);
    }

    @Test
    void testDisjointVectorSimilarityIsZero() {
        double[] a = service.tagsToVector("日系,校园");
        double[] b = service.tagsToVector("夜景,街拍");
        assertEquals(0.0, service.cosineSimilarity(a, b), 0.0001);
    }

    @Test
    void testPartialOverlapSimilarity() {
        double[] a = service.tagsToVector("日系,校园");
        double[] b = service.tagsToVector("日系,清新");
        double sim = service.cosineSimilarity(a, b);
        assertTrue(sim > 0 && sim < 1);
        // 两个向量各2个1，重叠1个，cos = 1/(sqrt2*sqrt2) = 0.5
        assertEquals(0.5, sim, 0.0001);
    }

    @Test
    void testEmptyTagsVector() {
        double[] a = service.tagsToVector("");
        double[] b = service.tagsToVector("日系");
        assertEquals(0.0, service.cosineSimilarity(a, b), 0.0001);
    }

    @Test
    void testTagDictHasTenTags() {
        assertEquals(10, service.getTagDict().size());
    }
}
