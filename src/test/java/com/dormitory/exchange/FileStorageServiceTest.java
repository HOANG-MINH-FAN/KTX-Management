package com.dormitory.exchange;

import com.dormitory.exchange.service.FileStorageService;
import com.dormitory.exchange.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl(tempDir.toString());
    }

    private byte[] createTestImageBytes(String format) throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        return baos.toByteArray();
    }

    @Test
    void testStoreValidJpegImage() throws Exception {
        byte[] bytes = createTestImageBytes("jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", bytes
        );

        String path = fileStorageService.storeImage(file);
        assertNotNull(path);
        assertTrue(path.startsWith("/uploads/exchange/"));
        assertTrue(path.endsWith(".jpg"));
        assertFalse(path.contains("photo.jpg")); // Tên file phải được sinh ngẫu nhiên an toàn
    }

    @Test
    void testStoreValidPngImage() throws Exception {
        byte[] bytes = createTestImageBytes("png");
        MockMultipartFile file = new MockMultipartFile(
                "image", "diagram.png", "image/png", bytes
        );

        String path = fileStorageService.storeImage(file);
        assertNotNull(path);
        assertTrue(path.endsWith(".png"));
    }

    @Test
    void testRejectInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "malicious.exe", "image/jpeg", "fakecontent".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeImage(file)
        );
        assertTrue(ex.getMessage().contains("Phần mở rộng file không được hỗ trợ"));
    }

    @Test
    void testRejectInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "script.sh.jpg", "text/plain", "echo hacked".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeImage(file)
        );
        assertTrue(ex.getMessage().contains("Định dạng file không hợp lệ"));
    }

    @Test
    void testRejectFakeImageDisguisedAsJpg() {
        // Tên đuôi .jpg, content-type image/jpeg, nhưng nội dung là text thường (không decode được ảnh)
        MockMultipartFile file = new MockMultipartFile(
                "image", "payload.jpg", "image/jpeg", "<?php echo 'hack'; ?>".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeImage(file)
        );
        assertTrue(ex.getMessage().contains("không phải là ảnh hợp lệ"));
    }

    @Test
    void testRejectFileSizeExceeding5MB() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "image", "huge.jpg", "image/jpeg", largeBytes
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeImage(file)
        );
        assertTrue(ex.getMessage().contains("vượt quá giới hạn cho phép"));
    }
}
