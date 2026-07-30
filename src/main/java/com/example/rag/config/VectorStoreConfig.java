package com.example.rag.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Cấu hình tự động đồng bộ và khắc phục mâu thuẫn Vector Dimension giữa PostgreSQL (pgvector)
 * và EmbeddingModel (Ollama 768 dimensions vs OpenAI 1536 dimensions).
 */
@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    /**
     * PostProcessor chạy trước khi PgVectorStore được khởi tạo.
     * Tự động kiểm tra số chiều (dimension) của bảng 'vector_store' hiện tại trên PostgreSQL/Supabase.
     * Nếu số chiều không khớp với EmbeddingModel đang active (ví dụ: CSDL là 1536 nhưng Model local là 768),
     * tự động DROP bảng cũ để PgVectorStore khởi tạo lại bảng mới với đúng dimension.
     */
    @Bean
    public static BeanPostProcessor vectorStoreSchemaInitializer(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new BeanPostProcessor() {
            private boolean initialized = false;

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (!initialized && ("vectorStore".equals(beanName) || beanName.toLowerCase().contains("vectorstore"))) {
                    initialized = true;
                    try {
                        alignVectorStoreDimension(jdbcTemplate, embeddingModel);
                    } catch (Exception e) {
                        log.warn("Không thể kiểm tra/điều chỉnh tự động schema vector_store: {}", e.getMessage());
                    }
                }
                return bean;
            }
        };
    }

    private static void alignVectorStoreDimension(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        try {
            // Lấy chiều thực tế của Embedding Model hiện tại (Ollama nomic-embed-text: 768, OpenAI: 1536)
            float[] sampleVector = embeddingModel.embed("test");
            int targetDimension = sampleVector.length;
            log.info("Mô hình EmbeddingModel đang hoạt động tạo vector số chiều = {}", targetDimension);

            // Kiểm tra xem bảng vector_store đã tồn tại trong PostgreSQL chưa
            Boolean tableExists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'vector_store')",
                    Boolean.class
            );

            if (Boolean.TRUE.equals(tableExists)) {
                // Lấy thông tin kiều dữ liệu thực tế của cột 'embedding' trong bảng 'vector_store' (ví dụ: 'vector(1536)' hoặc 'vector(768)')
                String formatType = jdbcTemplate.queryForObject(
                        "SELECT format_type(atttypid, atttypmod) FROM pg_attribute WHERE attrelid = 'vector_store'::regclass AND attname = 'embedding'",
                        String.class
                );

                String expectedType = "vector(" + targetDimension + ")";
                log.info("Kiểu dữ liệu cột embedding hiện tại trên DB: '{}', Kỳ vọng theo Model: '{}'", formatType, expectedType);

                if (formatType != null && !formatType.equalsIgnoreCase(expectedType)) {
                    log.warn("PHÁT HIỆN MÂU THUẪN DIMENSION! Bảng 'vector_store' có kiểu '{}', nhưng Model yêu cầu '{}'. Đang xóa bảng cũ để khởi tạo lại...",
                            formatType, expectedType);

                    jdbcTemplate.execute("DROP TABLE IF EXISTS vector_store CASCADE");
                    log.info("Đã DROP bảng 'vector_store' cũ thành công. PgVectorStore sẽ tự động tạo lại bảng mới với kiểu '{}'.", expectedType);
                } else {
                    log.info("Bảng 'vector_store' trên CSDL đã hoàn toàn tương thích với Model ({})", expectedType);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra/tự động đồng bộ schema vector_store: ", e);
        }
    }
}
