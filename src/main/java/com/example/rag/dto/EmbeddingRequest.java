package com.example.rag.dto;

/**
 * Request payload cho API Vector hóa văn bản.
 * 
 * @param text Đoạn văn bản cần chuyển thành Vector Embedding.
 */
public record EmbeddingRequest(String text) {
}
