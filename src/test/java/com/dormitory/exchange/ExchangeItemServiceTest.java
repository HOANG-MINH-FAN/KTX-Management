package com.dormitory.exchange;

import com.dormitory.entity.Student;
import com.dormitory.exchange.dto.ExchangeItemForm;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.FileStorageService;
import com.dormitory.exchange.service.impl.ExchangeItemServiceImpl;
import com.dormitory.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeItemServiceTest {

    @Mock
    private ExchangeItemRepository repository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FileStorageService fileStorageService;

    private ExchangeItemService service;

    private final Long OWNER_STUDENT_ID = 100L;
    private final Long OTHER_STUDENT_ID = 200L;

    @BeforeEach
    void setUp() {
        service = new ExchangeItemServiceImpl(
                repository,
                exchangeRequestRepository,
                studentRepository,
                fileStorageService
        );
    }

    @Test
    void testCreateItemSetsOwnerIdAndAvailableStatus() {
        ExchangeItemForm form = new ExchangeItemForm();
        form.setTitle("Nồi cơm điện Cuckoo");
        form.setCategory("Đồ gia dụng - Phòng ở");
        form.setExchangeType("SELL");
        form.setItemCondition("LIKE_NEW");
        form.setPrice(new BigDecimal("250000"));

        when(repository.save(any(ExchangeItem.class))).thenAnswer(invocation -> {
            ExchangeItem item = invocation.getArgument(0);
            return item;
        });

        ExchangeItem created = service.createItem(form, OWNER_STUDENT_ID, null);

        assertNotNull(created);
        assertEquals(OWNER_STUDENT_ID, created.getOwnerId());
        assertEquals("AVAILABLE", created.getStatus());
        assertEquals("Nồi cơm điện Cuckoo", created.getTitle());
        assertEquals("SELL", created.getExchangeType());
        assertEquals(new BigDecimal("250000"), created.getPrice());
        assertNull(created.getDesiredItem());
        verify(repository, times(1)).save(any(ExchangeItem.class));
    }

    @Test
    void testCreateGiveawayItemClearsPrice() {
        ExchangeItemForm form = new ExchangeItemForm();
        form.setTitle("Giáo trình Tiếng Anh KTX");
        form.setCategory("Đồ dùng học tập - Sách");
        form.setExchangeType("GIVEAWAY");
        form.setItemCondition("USED");
        form.setPrice(new BigDecimal("50000")); // Người dùng có thể lỡ nhập

        when(repository.save(any(ExchangeItem.class))).thenAnswer(i -> i.getArgument(0));

        ExchangeItem created = service.createItem(form, OWNER_STUDENT_ID, null);

        assertEquals("GIVEAWAY", created.getExchangeType());
        assertNull(created.getPrice(), "Hình thức GIVEAWAY phải không có giá");
    }

    @Test
    void testUpdateItemSuccessByOwner() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);
        existing.setTitle("Bàn học cũ");
        existing.setStatus("AVAILABLE");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(Collections.emptyList());
        when(repository.save(any(ExchangeItem.class))).thenAnswer(i -> i.getArgument(0));

        ExchangeItemForm form = new ExchangeItemForm();
        form.setTitle("Bàn học mới sửa");
        form.setCategory("Đồ gia dụng - Phòng ở");
        form.setExchangeType("EXCHANGE");
        form.setItemCondition("USED");
        form.setDesiredItem("Đổi lấy ghế xoay");

        ExchangeItem updated = service.updateItem(1L, form, OWNER_STUDENT_ID, null);

        assertEquals("Bàn học mới sửa", updated.getTitle());
        assertEquals("EXCHANGE", updated.getExchangeType());
        assertEquals("Đổi lấy ghế xoay", updated.getDesiredItem());
    }

    @Test
    void testUpdateItemRejectedForNonOwner() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        ExchangeItemForm form = new ExchangeItemForm();
        form.setTitle("Hack title");
        form.setCategory("Khác");
        form.setExchangeType("SELL");
        form.setItemCondition("NEW");

        SecurityException ex = assertThrows(SecurityException.class, () ->
                service.updateItem(1L, form, OTHER_STUDENT_ID, null)
        );
        assertTrue(ex.getMessage().contains("không có quyền chỉnh sửa"));
    }

    @Test
    void testUpdateItemRejectedWhenTransactionAcceptedOrCompleted() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);
        existing.setStatus("PENDING");

        ExchangeRequest req = new ExchangeRequest();
        req.setStatus("ACCEPTED");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(List.of(req));

        ExchangeItemForm form = new ExchangeItemForm();
        form.setTitle("Title");
        form.setCategory("Khác");
        form.setExchangeType("SELL");
        form.setItemCondition("NEW");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateItem(1L, form, OWNER_STUDENT_ID, null)
        );
        assertTrue(ex.getMessage().contains("đã có giao dịch được chấp nhận hoặc hoàn thành"));
    }

    @Test
    void testToggleHideAvailableToClosed() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);
        existing.setStatus("AVAILABLE");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        String newStatus = service.toggleHide(1L, OWNER_STUDENT_ID);

        assertEquals("CLOSED", newStatus);
        assertEquals("CLOSED", existing.getStatus());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testToggleHideClosedToAvailable() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);
        existing.setStatus("CLOSED");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        String newStatus = service.toggleHide(1L, OWNER_STUDENT_ID);

        assertEquals("AVAILABLE", newStatus);
        assertEquals("AVAILABLE", existing.getStatus());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testDeleteItemSuccessWhenNoRequests() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);
        existing.setImagePath("/uploads/exchange/sample.jpg");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        service.deleteItem(1L, OWNER_STUDENT_ID);

        verify(fileStorageService, times(1)).deleteImage("/uploads/exchange/sample.jpg");
        verify(repository, times(1)).delete(existing);
    }

    @Test
    void testDeleteItemRejectedWhenRequestsExist() {
        ExchangeItem existing = new ExchangeItem();
        existing.setOwnerId(OWNER_STUDENT_ID);

        ExchangeRequest req = new ExchangeRequest();
        req.setStatus("PENDING");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(exchangeRequestRepository.findByItemId(1L)).thenReturn(List.of(req));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.deleteItem(1L, OWNER_STUDENT_ID)
        );
        assertTrue(ex.getMessage().contains("Không thể xóa vĩnh viễn tin đăng đã có yêu cầu giao dịch"));
        verify(repository, never()).delete(any());
    }

    @Test
    void testGetOwnerStudentPhone() {
        Student student = new Student();
        student.setPhone("0987654321");
        student.setFullName("Nguyen Van A");

        when(studentRepository.findById(OWNER_STUDENT_ID)).thenReturn(Optional.of(student));

        Optional<Student> owner = service.getOwnerStudent(OWNER_STUDENT_ID);
        assertTrue(owner.isPresent());
        assertEquals("0987654321", owner.get().getPhone());
        assertEquals("Nguyen Van A", owner.get().getFullName());
    }
}
