package com.dormitory.exchange.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Lưu file ảnh tải lên một cách an toàn.
     * @param file File ảnh người dùng tải lên
     * @return Đường dẫn URL để truy cập ảnh (VD: /uploads/exchange/abc123xyz.jpg)
     */
    String storeImage(MultipartFile file);

    /**
     * Xóa ảnh cũ (nếu có) khi cập nhật hoặc xóa tin.
     * @param imagePath Đường dẫn URL của ảnh cần xóa
     */
    void deleteImage(String imagePath);
}
