package com.example.rag.dto;

import java.util.List;

/**
 * Response payload cho API Vector hóa văn bản.
 * 
 * @param text Đoạn văn bản gốc.
 * @param dimensions Số chiều của vector embedding (ví dụ: 1536 với OpenAI, 768 với Nomic Embed).
 * @param vector Mảng float biểu diễn vector không gian đa chiều.
 * @param samplePreview Trích xuất vài phần tử đầu tiên của vector để hiển thị UI.
 */
public record EmbeddingResponse(
        String text,
        int dimensions,
        float[] vector,
        List<Float> samplePreview
) {
}
