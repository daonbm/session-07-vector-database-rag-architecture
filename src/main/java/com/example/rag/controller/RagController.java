package com.example.rag.controller;

import com.example.rag.dto.IngestResponse;
import com.example.rag.dto.RagChatRequest;
import com.example.rag.dto.RagChatResponse;
import com.example.rag.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Lesson 03 & Lesson 04:
 * 
 * - POST /api/v1/rag/ingest: Ingest tài liệu nội bộ vào PGVector (Supabase).
 * - POST /api/v1/rag/chat: Hỏi đáp chuẩn kiến trúc RAG (Retrieval-Augmented Generation).
 * - GET  /api/v1/rag/search: Tìm kiếm dữ liệu tương đồng trực tiếp trong PGVector Store.
 */
@RestController
@RequestMapping("/api/v1/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Lesson 04 (Ingestion): API nạp và cắt nhỏ tài liệu vào Vector Store PGVector.
     */
    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingestDocument() {
        IngestResponse response = ragService.ingestSampleDocument();
        return ResponseEntity.ok(response);
    }

    /**
     * Lesson 04 (Retrieval & Generation): API Hỏi đáp RAG thời gian thực.
     */
    @PostMapping("/chat")
    public ResponseEntity<RagChatResponse> chat(@RequestBody RagChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        RagChatResponse response = ragService.askRagChatbot(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lesson 03 (Vector Search): API tìm kiếm các Chunks tương đồng trực tiếp từ PGVector Store.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Document>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "3") int topK) {
        List<Document> results = ragService.searchSimilarDocuments(query, topK);
        return ResponseEntity.ok(results);
    }
}
