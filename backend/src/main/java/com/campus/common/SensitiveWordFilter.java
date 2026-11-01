package com.campus.common;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 敏感词过滤器（基于 DFA 有限状态自动机）
 *
 * 用于需求、作品、评价、私信等内容的敏感词检测与过滤。
 * DFA 算法时间复杂度 O(n)，与敏感词库大小无关，适合高效实时过滤。
 */
@Component
public class SensitiveWordFilter {

    /** 敏感词根节点 */
    private final Map<Character, Object> root = new HashMap<>();

    /** 敏感词库（可按需扩充） */
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Set.of(
            "赌博", "博彩", "诈骗", "代考", "替考", "作弊", "枪支", "毒品",
            "嫖娼", "卖淫", "裸聊", "色情", "暴力", "反动", "邪教"
    ));

    public SensitiveWordFilter() {
        buildTrie();
    }

    /**
     * 构建 DFA 字典树
     */
    private void buildTrie() {
        for (String word : SENSITIVE_WORDS) {
            Map<Character, Object> node = root;
            for (char c : word.toCharArray()) {
                @SuppressWarnings("unchecked")
                Map<Character, Object> next = (Map<Character, Object>) node.computeIfAbsent(c, k -> new HashMap<Character, Object>());
                node = next;
            }
            // 词尾标记
            node.put('\0', Boolean.TRUE);
        }
    }

    /**
     * 检测文本是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int n = text.length();
        for (int i = 0; i < n; i++) {
            Map<Character, Object> node = root;
            for (int j = i; j < n; j++) {
                char c = text.charAt(j);
                @SuppressWarnings("unchecked")
                Map<Character, Object> next = (Map<Character, Object>) node.get(c);
                if (next == null) {
                    break;
                }
                node = next;
                if (node.containsKey('\0')) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回文本中命中的敏感词列表
     */
    public Set<String> match(String text) {
        Set<String> result = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        int n = text.length();
        for (int i = 0; i < n; i++) {
            Map<Character, Object> node = root;
            for (int j = i; j < n; j++) {
                char c = text.charAt(j);
                @SuppressWarnings("unchecked")
                Map<Character, Object> next = (Map<Character, Object>) node.get(c);
                if (next == null) {
                    break;
                }
                node = next;
                if (node.containsKey('\0')) {
                    result.add(text.substring(i, j + 1));
                }
            }
        }
        return result;
    }

    /**
     * 将敏感词替换为 *（脱敏）
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Set<String> matched = match(text);
        String result = text;
        for (String word : matched) {
            result = result.replace(word, "*".repeat(word.length()));
        }
        return result;
    }
}
