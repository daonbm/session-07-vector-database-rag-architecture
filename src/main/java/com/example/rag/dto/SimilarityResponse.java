package com.example.rag.dto;

/**
 * Response payload trả về điểm số độ tương đồng Cosine (Cosine Similarity Score).
 * 
 * @param text1 Văn bản 1.
 * @param text2 Văn bản 2.
 * @param cosineSimilarity Điểm số tương đồng từ -1.0 đến 1.0 (càng gần 1.0 càng giống nhau về ngữ nghĩa).
 * @param percentage Tỷ lệ phần trăm tương đồng (0% - 100%).
 * @param interpretation Đánh giá mức độ tương đồng (Rất giống, Khá tương đồng, Khác biệt...).
 */
public record SimilarityResponse(
        String text1,
        String text2,
        double cosineSimilarity,
        double percentage,
        String interpretation
) {
}
