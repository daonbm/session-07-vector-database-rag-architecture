package com.example.rag.service;

import com.example.rag.dto.IngestResponse;
import com.example.rag.dto.RagChatRequest;
import com.example.rag.dto.RagChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service thực thi quy trình RAG (Retrieval-Augmented Generation) hoàn chỉnh:
 * 
 * Lesson 03: Tích hợp Vector Database PGVector (Supabase)
 * Lesson 04: Quy trình Nạp tài liệu (Ingestion) & Hỏi đáp RAG (Retrieval & Generation)
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    @Value("classpath:documents/company_policy.md")
    private Resource sampleDocumentResource;

    public RagService(VectorStore vectorStore, ChatModel chatModel) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
    }

    /**
     * Quy trình Ingestion (Nạp tài liệu):
     * 1. Read: Đọc file văn bản/pdf nội bộ bằng TextReader/Tika.
     * 2. Chunking: Cắt nhỏ văn bản thành các đoạn (chunks) bằng TokenTextSplitter.
     * 3. Embed & Store: Tự động chuyển chunks thành vector và lưu trữ vào PGVector Store.
     * 
     * @return IngestResponse Thông báo số lượng chunks đã nạp thành công.
     */
    public IngestResponse ingestSampleDocument() {
        log.info("Bắt đầu quy trình Ingestion tài liệu mẫu: company_policy.md");

        try {
            // Bước 1: Đọc nội dung file tài liệu
            TextReader textReader = new TextReader(sampleDocumentResource);
            List<Document> rawDocuments = textReader.get();
            log.info("Đã đọc thành công tài liệu gốc với {} trang/phần.", rawDocuments.size());

            // Bước 2: Chunking (Phân đoạn văn bản)
            // TokenTextSplitter cắt văn bản theo số lượng token (mỗi chunk ~300 tokens, overlap ~50 tokens để giữ ngữ cảnh)
            TokenTextSplitter textSplitter = new TokenTextSplitter(300, 50, 5, 10000, true);
            List<Document> splitDocuments = textSplitter.apply(rawDocuments);
            log.info("Sau khi phân đoạn (Chunking), thu được {} đoạn văn bản nhỏ.", splitDocuments.size());

            // Gán thêm metadata nguồn tài liệu cho từng chunk
            for (Document doc : splitDocuments) {
                doc.getMetadata().put("source", "company_policy.md");
                doc.getMetadata().put("ingested_at", System.currentTimeMillis());
            }

            // Bước 3: Vectorize & Lưu trữ vào PGVector Database (Supabase)
            log.info("Đang tính toán Embedding và lưu {} chunks vào PGVector Store...", splitDocuments.size());
            vectorStore.add(splitDocuments);
            log.info("Nạp dữ liệu vào PGVector hoàn tất!");

            return new IngestResponse(
                    "company_policy.md",
                    splitDocuments.size(),
                    "Đã phân đoạn và lưu trữ thành công " + splitDocuments.size() + " chunks vào PGVector Store (Supabase)."
            );
        } catch (Exception e) {
            log.error("Lỗi trong quá trình Ingest tài liệu: ", e);
            throw new RuntimeException("Không thể nạp tài liệu vào Vector Store: " + e.getMessage(), e);
        }
    }

    /**
     * Quy trình Retrieval & Generation (Hỏi đáp RAG dựa trên tài liệu):
     * 1. Retrieval: Vector hóa câu hỏi, thực hiện Similarity Search trên PGVector để lấy các chunks liên quan nhất.
     * 2. Context Building: Ghép các chunks thu được thành khối ngữ cảnh (Context Block).
     * 3. Augmented Generation: Đưa Context và Question vào Prompt Template, gọi LLM sinh câu trả lời.
     * 
     * @param request Chứa câu hỏi của người dùng và TopK cần trích xuất.
     * @return RagChatResponse Kết quả câu trả lời cùng danh sách trích dẫn ngữ cảnh (Sources).
     */
    public RagChatResponse askRagChatbot(RagChatRequest request) {
        String question = request.question();
        int topK = request.topK();
        log.info("Xử lý RAG Request với câu hỏi: '{}', TopK: {}", question, topK);

        // Bước 1: Retrieval - Tìm kiếm TopK đoạn văn bản có độ tương đồng cao nhất trong PGVector
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(0.3) // Ngưỡng độ tương đồng tối thiểu
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        log.info("PGVector tìm thấy {} đoạn ngữ cảnh tương đồng.", similarDocuments.size());

        // Bước 2: Context Building - Tổng hợp các chunks thành văn bản ngữ cảnh
        String contextText = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--- DỌAN TRÍCH NGUỒN ---\n\n"));

        if (similarDocuments.isEmpty()) {
            contextText = "Không tìm thấy thông tin phù hợp trong cơ sở dữ liệu nội bộ.";
        }

        // Bước 3: Prompt Engineering - Ràng buộc LLM chỉ trả lời dựa trên ngữ cảnh cung cấp
        String systemPromptMessage = """
                Bạn là một trợ lý AI nội bộ chuyên nghiệp của công ty. 
                Nhiệm vụ của bạn là trả lời câu hỏi của người dùng CHỈ DỰA TRÊN các đoạn thông tin ngữ cảnh được cung cấp dưới đây.
                
                QUY TẮC RÀNG BUỘC:
                1. Nếu thông tin không có trong ngữ cảnh, hãy trả lời lịch sự rằng: "Xin lỗi, tài liệu nội bộ hiện tại không đề cập đến thông tin này."
                2. Không tự bịa đặt hoặc sử dụng kiến thức bên ngoài ngữ cảnh được cung cấp.
                3. Trả lời ngắn gọn, chính xác, rõ ràng và lịch sự bằng Tiếng Việt.
                
                --- NGỮ CẢNH NỘI BỘ (CONTEXT) ---
                {context}
                
                --- CÂU HỎI NGƯỜI DÙNG ---
                {question}
                """;

        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemPromptMessage);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", contextText,
                "question", question
        ));

        // Bước 4: Gọi LLM (Ollama hoặc OpenAI tùy profile) để tổng hợp câu trả lời
        log.info("Gửi Prompt đến LLM...");
        String answer = chatModel.call(prompt).getResult().getOutput().getText();

        List<RagChatResponse.ContextSource> sources = similarDocuments.stream()
                .map(doc -> {
                    Object distObj = doc.getMetadata().get("distance");
                    float score = (distObj instanceof Number n) ? n.floatValue() : 0.0f;
                    return new RagChatResponse.ContextSource(
                            doc.getText(),
                            doc.getMetadata(),
                            score
                    );
                })
                .collect(Collectors.toList());

        return new RagChatResponse(question, answer, sources);
    }

    /**
     * Phương thức phụ trợ phục vụ tìm kiếm trực tiếp trong Vector Store.
     */
    public List<Document> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build()
        );
    }
}
