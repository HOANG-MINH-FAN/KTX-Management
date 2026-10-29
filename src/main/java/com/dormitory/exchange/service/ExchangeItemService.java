package com.dormitory.exchange.service;

import com.dormitory.entity.Student;
import com.dormitory.exchange.dto.ExchangeItemForm;
import com.dormitory.exchange.entity.ExchangeItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ExchangeItemService {

    List<ExchangeItem> getAll();

    List<ExchangeItem> getAvailable();

    Optional<ExchangeItem> getById(Long id);

    ExchangeItem save(ExchangeItem item);

    void delete(Long id);

    /**
     * Lấy danh sách tin đăng của một sinh viên cụ thể (sắp xếp mới nhất trước).
     */
    List<ExchangeItem> getByOwnerId(Long ownerId);

    /**
     * Sinh viên đăng tin mới kèm ảnh.
     */
    ExchangeItem createItem(ExchangeItemForm form, Long studentId, MultipartFile image);

    /**
     * Sinh viên sửa tin của chính mình kèm ảnh mới (nếu có).
     */
    ExchangeItem updateItem(Long itemId, ExchangeItemForm form, Long studentId, MultipartFile image);

    /**
     * Ẩn hoặc hiện lại tin (đổi status giữa AVAILABLE và CLOSED).
     * @return trạng thái mới của tin
     */
    String toggleHide(Long itemId, Long studentId);

    /**
     * Xóa vĩnh viễn tin đăng (chỉ cho phép khi chưa có yêu cầu giao dịch).
     */
    void deleteItem(Long itemId, Long studentId);

    /**
     * Kiểm tra xem tin đăng đã có bất kỳ yêu cầu giao dịch nào chưa.
     */
    boolean hasExchangeRequests(Long itemId);

    /**
     * Kiểm tra xem tin đã có giao dịch được chấp nhận hoặc hoàn thành chưa.
     */
    boolean hasAcceptedOrCompletedTransaction(Long itemId);

    /**
     * Lấy thông tin sinh viên chủ tin (bao gồm phone) từ Student entity.
     */
    Optional<Student> getOwnerStudent(Long ownerId);
}