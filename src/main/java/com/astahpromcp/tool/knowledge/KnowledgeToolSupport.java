package com.astahpromcp.tool.knowledge;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeToolSupport {

    public static List<String> splitText(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }

        return chunks;
    }
}
