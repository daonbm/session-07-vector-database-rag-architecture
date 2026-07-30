package com.example.rag.controller;

import com.example.rag.dto.EmbeddingRequest;
import com.example.rag.dto.EmbeddingResponse;
import com.example.rag.dto.SimilarityRequest;
import com.example.rag.dto.SimilarityResponse;
import com.example.rag.service.EmbeddingDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Lesson 01 & Lesson 02:
 * 
 * - POST /api/v1/embedding/embed: Vector hóa một đoạn văn bản thành mảng Float Embedding.
 * - POST /api/v1/embedding/similarity: So sánh độ tương đồng Cosine giữa 2 đoạn văn bản.
 */
@RestController
@RequestMapping("/api/v1/embedding")
@CrossOrigin(origins = "*")
public class EmbeddingController {

    private final EmbeddingDemoService embeddingDemoService;

    public EmbeddingController(EmbeddingDemoService embeddingDemoService) {
        this.embeddingDemoService = embeddingDemoService;
    }

    /**
     * Lesson 01: API chuyển văn bản thành Vector Embedding.
     */
    @PostMapping("/embed")
    public ResponseEntity<EmbeddingResponse> generateEmbedding(@RequestBody EmbeddingRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        EmbeddingResponse response = embeddingDemoService.generateEmbedding(request.text());
        return ResponseEntity.ok(response);
    }

    /**
     * Lesson 02: API tính độ tương đồng Cosine giữa 2 đoạn văn bản.
     */
    @PostMapping("/similarity")
    public ResponseEntity<SimilarityResponse> calculateSimilarity(@RequestBody SimilarityRequest request) {
        if (request.text1() == null || request.text2() == null) {
            return ResponseEntity.badRequest().build();
        }
        SimilarityResponse response = embeddingDemoService.calculateSimilarity(request.text1(), request.text2());
        return ResponseEntity.ok(response);
    }
}
