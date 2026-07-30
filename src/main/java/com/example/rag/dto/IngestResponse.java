package com.example.rag.dto;

/**
 * Response payload sau khi nạp tài liệu vào PGVector Vector Store.
 * 
 * @param fileName Tên tài liệu đã được nạp.
 * @param totalChunks Số lượng đoạn nhỏ (chunks) đã cắt.
 * @param message Thông báo kết quả thành công.
 */
public record IngestResponse(
        String fileName,
        int totalChunks,
        String message
) {
}
