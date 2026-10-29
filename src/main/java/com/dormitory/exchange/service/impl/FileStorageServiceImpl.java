package com.dormitory.exchange.service.impl;

import com.dormitory.exchange.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Path uploadLocation;

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads/exchange}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ ảnh: " + this.uploadLocation, e);
        }
    }

    @Override
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. Kiểm tra dung lượng file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dung lượng ảnh vượt quá giới hạn cho phép (tối đa 5MB).");
        }

        // 2. Kiểm tra MIME type gửi kèm
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Định dạng file không hợp lệ. Chỉ chấp nhận JPG, JPEG, PNG, WEBP, GIF.");
        }

        // 3. Trích xuất phần mở rộng an toàn (không tin cậy tên file người dùng gửi)
        String originalFilename = file.getOriginalFilename();
        String extension = extractSafeExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Phần mở rộng file không được hỗ trợ. Chỉ chấp nhận .jpg, .jpeg, .png, .webp, .gif.");
        }

        // 4. Kiểm tra nội dung thực sự của ảnh bằng ImageIO (chống tệp mã độc giả danh ảnh)
        // Lưu ý: với một số tệp webp, ImageIO cơ bản có thể cần plugin nếu không decode được, nên kiểm tra luồng cẩn thận
        try (InputStream is = file.getInputStream()) {
            if (!extension.equals(".webp")) {
                BufferedImage bi = ImageIO.read(is);
                if (bi == null) {
                    throw new IllegalArgumentException("Nội dung tệp tin không phải là ảnh hợp lệ.");
                }
            } else {
                // Với webp, kiểm tra kích thước tối thiểu và header bytes 'RIFF' .... 'WEBP'
                byte[] header = new byte[12];
                int read = is.read(header);
                if (read < 12 || !(header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P')) {
                    throw new IllegalArgumentException("Nội dung tệp WEBP không hợp lệ.");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc tệp ảnh tải lên: " + e.getMessage());
        }

        // 5. Sinh tên file ngẫu nhiên an toàn tuyệt đối
        String safeFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path targetPath = this.uploadLocation.resolve(safeFileName).normalize();
            // Đảm bảo đường dẫn nằm trong thư mục cho phép (chống Directory Traversal)
            if (!targetPath.startsWith(this.uploadLocation)) {
                throw new SecurityException("Phát hiện đường dẫn không an toàn.");
            }

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/exchange/" + safeFileName;
        } catch (IOException e) {
            throw new RuntimeException("Lưu file ảnh thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imagePath) {
        if (imagePath == null || !imagePath.startsWith("/uploads/exchange/")) {
            return;
        }
        try {
            String filename = imagePath.substring("/uploads/exchange/".length());
            Path targetPath = this.uploadLocation.resolve(filename).normalize();
            if (targetPath.startsWith(this.uploadLocation)) {
                Files.deleteIfExists(targetPath);
            }
        } catch (IOException ignored) {
            // Không ngắt tiến trình nếu xóa file vật lý thất bại
        }
    }

    private String extractSafeExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return null;
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
