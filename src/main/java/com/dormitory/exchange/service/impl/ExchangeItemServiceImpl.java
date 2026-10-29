package com.dormitory.exchange.service.impl;

import com.dormitory.entity.Student;
import com.dormitory.exchange.dto.ExchangeItemForm;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.FileStorageService;
import com.dormitory.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeItemServiceImpl implements ExchangeItemService {

    private final ExchangeItemRepository repository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public ExchangeItemServiceImpl(ExchangeItemRepository repository,
                                   ExchangeRequestRepository exchangeRequestRepository,
                                   StudentRepository studentRepository,
                                   FileStorageService fileStorageService) {
        this.repository = repository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<ExchangeItem> getAll() {
        return repository.findAll();
    }

    @Override
    public List<ExchangeItem> getAvailable() {
        return repository.findByStatus("AVAILABLE");
    }

    @Override
    public Optional<ExchangeItem> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ExchangeItem save(ExchangeItem item) {
        return repository.save(item);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ExchangeItem> getByOwnerId(Long ownerId) {
        return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    @Transactional
    public ExchangeItem createItem(ExchangeItemForm form, Long studentId, MultipartFile image) {
        ExchangeItem item = new ExchangeItem();
        item.setOwnerId(studentId);
        item.setStatus("AVAILABLE");

        applyFormDataToItem(form, item);

        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeImage(image);
            item.setImagePath(imagePath);
        }

        return repository.save(item);
    }

    @Override
    @Transactional
    public ExchangeItem updateItem(Long itemId, ExchangeItemForm form, Long studentId, MultipartFile image) {
        ExchangeItem item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng với mã: " + itemId));

        if (!item.getOwnerId().equals(studentId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa tin đăng của người khác.");
        }

        if (hasAcceptedOrCompletedTransaction(itemId)) {
            throw new IllegalStateException("Không thể chỉnh sửa tin đăng đã có giao dịch được chấp nhận hoặc hoàn thành.");
        }

        applyFormDataToItem(form, item);

        if (image != null && !image.isEmpty()) {
            String newImagePath = fileStorageService.storeImage(image);
            if (item.getImagePath() != null) {
                fileStorageService.deleteImage(item.getImagePath());
            }
            item.setImagePath(newImagePath);
        }

        return repository.save(item);
    }

    @Override
    @Transactional
    public String toggleHide(Long itemId, Long studentId) {
        ExchangeItem item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng với mã: " + itemId));

        if (!item.getOwnerId().equals(studentId)) {
            throw new SecurityException("Bạn không có quyền thao tác trên tin đăng này.");
        }

        if (hasAcceptedOrCompletedTransaction(itemId)) {
            throw new IllegalStateException("Không thể ẩn/hiện tin đăng đã có giao dịch được chấp nhận hoặc hoàn thành.");
        }

        String currentStatus = item.getStatus();
        String nextStatus;
        if ("AVAILABLE".equalsIgnoreCase(currentStatus)) {
            nextStatus = "CLOSED";
        } else if ("CLOSED".equalsIgnoreCase(currentStatus)) {
            nextStatus = "AVAILABLE";
        } else {
            throw new IllegalStateException("Chỉ có thể ẩn hoặc mở lại tin đăng ở trạng thái AVAILABLE hoặc CLOSED.");
        }

        item.setStatus(nextStatus);
        repository.save(item);
        return nextStatus;
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long studentId) {
        ExchangeItem item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng với mã: " + itemId));

        if (!item.getOwnerId().equals(studentId)) {
            throw new SecurityException("Bạn không có quyền xóa tin đăng này.");
        }

        if (hasExchangeRequests(itemId)) {
            throw new IllegalStateException("Không thể xóa vĩnh viễn tin đăng đã có yêu cầu giao dịch. Bạn chỉ có thể chọn Ẩn tin.");
        }

        if (hasAcceptedOrCompletedTransaction(itemId)) {
            throw new IllegalStateException("Không thể xóa tin đăng đã có giao dịch được chấp nhận hoặc hoàn thành.");
        }

        if (item.getImagePath() != null) {
            fileStorageService.deleteImage(item.getImagePath());
        }

        repository.delete(item);
    }

    @Override
    public boolean hasExchangeRequests(Long itemId) {
        List<ExchangeRequest> requests = exchangeRequestRepository.findByItemId(itemId);
        return requests != null && !requests.isEmpty();
    }

    @Override
    public boolean hasAcceptedOrCompletedTransaction(Long itemId) {
        ExchangeItem item = repository.findById(itemId).orElse(null);
        if (item != null) {
            String status = item.getStatus();
            if ("SOLD".equalsIgnoreCase(status) || "EXCHANGED".equalsIgnoreCase(status)) {
                return true;
            }
        }

        List<ExchangeRequest> requests = exchangeRequestRepository.findByItemId(itemId);
        if (requests == null || requests.isEmpty()) {
            return false;
        }

        return requests.stream().anyMatch(r ->
                "ACCEPTED".equalsIgnoreCase(r.getStatus()) || "COMPLETED".equalsIgnoreCase(r.getStatus())
        );
    }

    @Override
    public Optional<Student> getOwnerStudent(Long ownerId) {
        return studentRepository.findById(ownerId);
    }

    private void applyFormDataToItem(ExchangeItemForm form, ExchangeItem item) {
        item.setTitle(form.getTitle().trim());
        item.setDescription(form.getDescription() != null ? form.getDescription().trim() : null);
        item.setCategory(form.getCategory().trim());
        item.setExchangeType(form.getExchangeType().trim().toUpperCase());
        item.setItemCondition(form.getItemCondition().trim().toUpperCase());

        String type = item.getExchangeType();
        if ("SELL".equalsIgnoreCase(type)) {
            item.setPrice(form.getPrice());
            item.setDesiredItem(null);
        } else if ("EXCHANGE".equalsIgnoreCase(type)) {
            item.setDesiredItem(form.getDesiredItem() != null ? form.getDesiredItem().trim() : null);
            item.setPrice(null);
        } else if ("GIVEAWAY".equalsIgnoreCase(type)) {
            item.setPrice(null);
            item.setDesiredItem(null);
        }
    }
}