package com.campus.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词过滤器（DFA）单元测试
 */
class SensitiveWordFilterTest {

    private final SensitiveWordFilter filter = new SensitiveWordFilter();

    @Test
    void testContainsSensitiveWord() {
        assertTrue(filter.contains("帮人代考英语"));
        assertTrue(filter.contains("这是赌博信息"));
        assertFalse(filter.contains("正常约拍内容"));
    }

    @Test
    void testMatchReturnsSensitiveWords() {
        var matched = filter.match("提供代考和作弊服务");
        assertTrue(matched.contains("代考"));
        assertTrue(matched.contains("作弊"));
    }

    @Test
    void testFilterReplacesWithStars() {
        String result = filter.filter("帮人代考");
        assertEquals("帮人**", result);
    }

    @Test
    void testEmptyText() {
        assertFalse(filter.contains(""));
        assertFalse(filter.contains(null));
    }
}
