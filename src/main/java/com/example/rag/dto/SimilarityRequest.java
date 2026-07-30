package com.example.rag.dto;

/**
 * Request payload tính toán độ tương đồng giữa 2 đoạn văn bản.
 * 
 * @param text1 Đoạn văn bản thứ nhất.
 * @param text2 Đoạn văn bản thứ hai.
 */
public record SimilarityRequest(
        String text1,
        String text2
) {
}
