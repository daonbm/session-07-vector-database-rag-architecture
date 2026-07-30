# Session 07: Vector Database & Kiến Trúc RAG (Retrieval-Augmented Generation)

Dự án mẫu Spring Boot hoàn chỉnh sử dụng Gradle và Spring AI nhằm giảng dạy & minh họa thực hành cho chủ đề **Vector Database (PGVector / Supabase)** và **Kiến trúc RAG (Hỏi đáp dựa trên tài liệu)**.

---

## 🎯 Mục Tiêu Bài Học & Tính Năng Key

1. **Lesson 01 - Vector Hóa Văn Bản (Text Embeddings):**
   - Chuyển đổi văn bản thành mảng số thực float array nằm trong không gian đa chiều bằng `EmbeddingModel`.
2. **Lesson 02 - Đo Độ Tương Đồng Ngữ Nghĩa (Cosine Similarity):**
   - Thuật toán đại số tuyến tính thủ công tính góc Cosine $\cos(\theta) = \frac{A \cdot B}{\|A\| \|B\|}$ đo mức độ trùng khớp ý nghĩa giữa 2 câu.
3. **Lesson 03 - Tích Hợp Vector Database PGVector (Supabase):**
   - Cấu hình `PgVectorStore` lưu trữ vector embedding trực tiếp trên cơ sở dữ liệu PostgreSQL đám mây Supabase.
4. **Lesson 04 - Xây Dựng RAG Chatbot Dựa Trên Tài Liệu Nội Bộ:**
   - **Ingestion:** Phân đoạn tài liệu bằng `TokenTextSplitter` và tự động lưu vector vào Supabase.
   - **Retrieval & Generation:** Truy vấn Top-K chunks tương đồng nhất từ Supabase, đưa vào System Prompt và gọi LLM trả lời chuẩn xác.

---

## 🛠️ Cấu Trúc Dự Án (Project Structure)

```text
session-07-vector-database-rag-architecture/
├── build.gradle                       # Cấu hình Gradle & Spring AI Dependencies
├── settings.gradle
├── src/main/
│   ├── java/com/example/rag/
│   │   ├── RagApplication.java        # Spring Boot Entry Point
│   │   ├── config/
│   │   │   └── VectorStoreConfig.java # Cấu hình Bean PgVectorStore
│   │   ├── dto/                       # Request/Response Data Objects
│   │   │   ├── EmbeddingRequest.java
│   │   │   ├── EmbeddingResponse.java
│   │   │   ├── SimilarityRequest.java
│   │   │   ├── SimilarityResponse.java
│   │   │   ├── RagChatRequest.java
│   │   │   ├── RagChatResponse.java
│   │   │   └── IngestResponse.java
│   │   ├── service/
│   │   │   ├── EmbeddingDemoService.java # Logic Lesson 1 & 2
│   │   │   └── RagService.java        # Logic Lesson 3 & 4 (Ingestion & RAG)
│   │   └── controller/
│   │       ├── EmbeddingController.java
│   │       └── RagController.java
│   └── resources/
│       ├── application.yml            # Dynamic Profiles & Database Config
│       ├── application-local.yml      # Profile Local: Ollama
│       ├── application-cloud.yml      # Profile Cloud: OpenAI / OpenRouter
│       ├── documents/
│       │   └── company_policy.md      # Tài liệu nội bộ mẫu
│       └── static/
│           └── index.html             # Web UI Dashboard trực quan
└── README.md
```

---

## ⚙️ Cấu Hồi Đa Môi Trường (Profiles Switch)

### 1. Môi Trường Local (Ollama)
Cấu hình trong `application-local.yml`:
- Ollama Base URL: `http://localhost:11434`
- Chat Model: `qwen2.5:1.5b`
- Embedding Model: `nomic-embed-text`

Khởi chạy bằng Gradle:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. Môi Trường Cloud (OpenAI / OpenRouter / 9Router)
Cấu hình trong `application-cloud.yml`:
- AI Base URL: `${AI_BASE_URL:http://localhost:20128}`
- API Key: `${AI_API_KEY:your-key}`
- Chat Model: `ag/gemini-2.5-flash`
- Embedding Model: `text-embedding-3-small`

Khởi chạy bằng Gradle:
```bash
./gradlew bootRun --args='--spring.profiles.active=cloud'
```

---

## 🗄️ Cấu Hồi PostgreSQL PGVector (Supabase)

Trước khi chạy ứng dụng, bạn cần kích hoạt Extension `vector` trên Supabase/PostgreSQL:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Cập nhật biến môi trường kết nối trong `application.yml` hoặc truyền khi khởi chạy:
- `DB_HOST`: Host Supabase PostgreSQL (ví dụ `db.xxxx.supabase.co`)
- `DB_USER`: `postgres`
- `DB_PASSWORD`: Mật khẩu Supabase của bạn

---

## 🚀 Danh Sách REST APIs

| Lesson | HTTP Method | Endpoint | Mô Tả |
| :--- | :--- | :--- | :--- |
| **Lesson 01** | `POST` | `/api/v1/embedding/embed` | Vector hóa chuỗi văn bản thành mảng Float Array |
| **Lesson 02** | `POST` | `/api/v1/embedding/similarity` | Tính độ tương đồng Cosine giữa 2 chuỗi |
| **Lesson 03 & 04** | `POST` | `/api/v1/rag/ingest` | Nạp & Chunking tài liệu mẫu vào PGVector |
| **Lesson 03** | `GET` | `/api/v1/rag/search?query=...&topK=3` | Tìm kiếm trực tiếp các Chunks trong PGVector |
| **Lesson 04** | `POST` | `/api/v1/rag/chat` | Hỏi đáp Chatbot theo quy trình RAG |

---

## 🖥️ Giao Diện Web Dashboard

Mở trình duyệt tại đường dẫn: `http://localhost:8080/index.html` để trải nghiệm Web UI trực quan!
