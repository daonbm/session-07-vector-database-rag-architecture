package com.example.rag.service;

import com.example.rag.dto.EmbeddingResponse;
import com.example.rag.dto.SimilarityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service minh họa cho Lesson 01 & Lesson 02:
 * 
 * 1. Lesson 01: Vector hóa văn bản (Text Embedding)
 *    Chuyển đổi chuỗi văn bản thành mảng số thực (float array) nằm trong không gian n-chiều.
 * 
 * 2. Lesson 02: Tính độ tương đồng góc Cosine (Cosine Similarity)
 *    Sử dụng công thức đại số tuyến tính để đo khoảng cách ngữ nghĩa giữa 2 vector.
 */
@Service
public class EmbeddingDemoService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingDemoService.class);

    private final EmbeddingModel embeddingModel;

    public EmbeddingDemoService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Lesson 01: Chuyển đổi đoạn văn bản thành Vector Embedding.
     * 
     * @param text Văn bản cần vector hóa.
     * @return EmbeddingResponse chứa vector float và thông tin kích thước chiều.
     */
    public EmbeddingResponse generateEmbedding(String text) {
        log.info("Generating embedding for text: '{}'", text);

        // Gọi Spring AI EmbeddingModel để chuyển text thành vector float[]
        float[] vector = embeddingModel.embed(text);
        int dimensions = vector.length;

        // Trích xuất 10 phần tử đầu tiên để làm mẫu xem trước trên UI
        List<Float> samplePreview = new ArrayList<>();
        int sampleSize = Math.min(10, dimensions);
        for (int i = 0; i < sampleSize; i++) {
            samplePreview.add(vector[i]);
        }

        log.info("Embedding generated successfully. Dimensions: {}", dimensions);
        return new EmbeddingResponse(text, dimensions, vector, samplePreview);
    }

    /**
     * Lesson 02: Tính điểm số tương đồng Cosine giữa 2 chuỗi văn bản bất kỳ.
     * 
     * Công thức Cosine Similarity:
     * cos(theta) = (A . B) / (||A|| * ||B||)
     *            = sum(A[i] * B[i]) / ( sqrt(sum(A[i]^2)) * sqrt(sum(B[i]^2)) )
     * 
     * @param text1 Chuỗi văn bản 1.
     * @param text2 Chuỗi văn bản 2.
     * @return SimilarityResponse chứa điểm số Cosine (-1.0 đến 1.0) và phân tích.
     */
    public SimilarityResponse calculateSimilarity(String text1, String text2) {
        log.info("Calculating Cosine Similarity between text1: '{}' and text2: '{}'", text1, text2);

        // Bước 1: Vector hóa cả 2 văn bản bằng EmbeddingModel
        float[] vector1 = embeddingModel.embed(text1);
        float[] vector2 = embeddingModel.embed(text2);

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Hai vector có kích thước không tương thích: " 
                    + vector1.length + " vs " + vector2.length);
        }

        // Bước 2: Tính Tích vô hướng (Dot Product) A . B
        double dotProduct = 0.0;
        // Bước 3: Tính Độ dài (Magnitude / Euclidean norm) ||A|| và ||B||
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += vector1[i] * vector1[i];
            normB += vector2[i] * vector2[i];
        }

        double magnitudeA = Math.sqrt(normA);
        double magnitudeB = Math.sqrt(normB);

        // Tránh chia cho 0
        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return new SimilarityResponse(text1, text2, 0.0, 0.0, "Vector không hợp lệ (độ dài bằng 0)");
        }

        // Điểm tương đồng Cosine Similarity
        double cosineSimilarity = dotProduct / (magnitudeA * magnitudeB);

        // Quy đổi ra phần trăm (chuyển khoảng [-1, 1] thành [0%, 100%] cho trực quan)
        double percentage = Math.max(0.0, cosineSimilarity) * 100.0;

        // Đánh giá ngữ nghĩa bằng lời
        String interpretation = interpretSimilarityScore(cosineSimilarity);

        log.info("Cosine Similarity result: {}, Percentage: {}%", cosineSimilarity, percentage);
        return new SimilarityResponse(text1, text2, cosineSimilarity, percentage, interpretation);
    }

    /**
     * Hàm phụ trợ giải thích ý nghĩa của chỉ số Cosine Similarity cho học viên.
     */
    private String interpretSimilarityScore(double score) {
        if (score >= 0.85) {
            return "Rất giống nhau (Ngữ nghĩa gần như trùng khớp hoàn toàn)";
        } else if (score >= 0.65) {
            return "Khá tương đồng (Có chung chủ đề hoặc ý tưởng chính)";
        } else if (score >= 0.40) {
            return "Có sự liên quan nhẹ (Chứa một số từ khóa hoặc ngữ cảnh chung)";
        } else {
            return "Rất ít liên quan / Khác biệt hoàn toàn về chủ đề";
        }
    }
}
