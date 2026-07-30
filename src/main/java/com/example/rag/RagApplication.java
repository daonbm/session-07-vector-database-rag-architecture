package com.example.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Entry Point cho Session 07: Vector Database & Kiến trúc RAG.
 * 
 * Ứng dụng tích hợp Spring AI với PGVector (Supabase) và hỗ trợ 2 profiles:
 * - local: Sử dụng Ollama (Local LLM & Embedding)
 * - cloud: Sử dụng OpenAI / OpenRouter (Cloud LLM & Embedding)
 */
@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
