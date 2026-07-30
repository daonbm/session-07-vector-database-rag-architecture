package com.example.rag.dto;

/**
 * Request payload cho API RAG Chatbot.
 * 
 * @param question Câu hỏi của người dùng.
 * @param topK Số lượng tài liệu liên quan nhất cần trích xuất từ Vector Database (mặc định: 3).
 */
public record RagChatRequest(
        String question,
        Integer topK
) {
    public RagChatRequest {
        if (topK == null || topK <= 0) {
            topK = 3;
        }
    }
}
