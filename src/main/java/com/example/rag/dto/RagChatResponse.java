package com.example.rag.dto;

import java.util.List;

/**
 * Response payload của RAG Chatbot chứa câu trả lời và danh sách ngữ cảnh tài liệu đã dùng.
 * 
 * @param question Câu hỏi gốc của người dùng.
 * @param answer Câu trả lời do LLM sinh ra dựa trên ngữ cảnh trích xuất.
 * @param retrievedContexts Danh sách các đoạn văn bản (chunks) được PGVector trả về kèm điểm tương đồng.
 */
public record RagChatResponse(
        String question,
        String answer,
        List<ContextSource> retrievedContexts
) {
    /**
     * Thông tin chi tiết một nguồn ngữ cảnh được trích xuất từ Vector Store.
     * 
     * @param content Nội dung đoạn văn bản (chunk).
     * @param metadata Các thuộc tính đi kèm (tên file, vị trí, id...).
     * @param score Điểm số tương đồng (similarity score) đối với câu hỏi.
     */
    public record ContextSource(
            String content,
            Object metadata,
            Float score
    ) {}
}
