package cn.lollipop.zhihu.service;

import org.apache.commons.lang3.CharUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {
    private final String DEFAULT_REPLACEMENT = "***";

    private TrieNode root = new TrieNode();

    // 判断是否是一个符号
    private boolean isSymbol(char c) {
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode {
        boolean end = false;
        Map<Character, TrieNode> subNode = new HashMap<>();

        boolean isEnd() {
            return end;
        }

        void addSubNode(Character c, TrieNode node) {
            subNode.put(c, node);
        }

        TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }

        public void setEnd() {
            end = true;
        }
    }

    public String filter(String text) {
        StringBuilder sb = new StringBuilder();
        int begin = 0;
        int pos = 0;
        TrieNode node = root;
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (isSymbol(c)) {
                if (pos == begin) {
                    sb.append(c);
                    begin++;
                }
                pos++;
                continue;
            }
            node = node.getSubNode(c);

            if (node == null) {
                sb.append(text.charAt(begin++));
                pos = begin;
                node = root;
            } else if (node.isEnd()) {
                sb.append(DEFAULT_REPLACEMENT);
                pos++;
                begin = pos;
                node = root;
            } else {
                pos++;
            }
        }
        return sb.toString();
    }

    @Override
    public void afterPropertiesSet() {
        root = new TrieNode();
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                addWord(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addWord(String text) {
        TrieNode node = root;

        for (int i = 0; i < text.length(); i++) {
            if (node.getSubNode(text.charAt(i)) == null) {
                node.addSubNode(text.charAt(i), new TrieNode());
            }
            node = node.getSubNode(text.charAt(i));
            if (i == text.length() - 1) {
                node.setEnd();
            }
        }
    }
}
