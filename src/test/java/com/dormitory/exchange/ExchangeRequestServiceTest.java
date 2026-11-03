package com.dormitory.exchange;

import com.dormitory.entity.Student;
import com.dormitory.exchange.dto.ExchangeRequestView;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.service.ExchangeRequestService;
import com.dormitory.exchange.service.impl.ExchangeRequestServiceImpl;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRequestServiceTest {

    @Mock
    private ExchangeRequestRepository requestRepository;

    @Mock
    private ExchangeItemRepository itemRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private NotificationService notificationService;

    private ExchangeRequestService service;

    private final Long OWNER_ID = 100L;
    private final Long REQUESTER_A_ID = 200L;
    private final Long REQUESTER_B_ID = 300L;
    private final Long OUTSIDER_ID = 400L;
    private final Long ITEM_ID = 10L;

    private Student ownerStudent;
    private Student requesterA;
    private Student requesterB;
    private ExchangeItem availableSellItem;

    @BeforeEach
    void setUp() {
        service = new ExchangeRequestServiceImpl(
                requestRepository,
                itemRepository,
                studentRepository,
                notificationService
        );

        ownerStudent = createStudent(OWNER_ID, "sv_owner", "SV001", "Nguyen Chu Tin", "0901111111");
        requesterA = createStudent(REQUESTER_A_ID, "sv_req_a", "SV002", "Tran Nguoi Mua A", "0902222222");
        requesterB = createStudent(REQUESTER_B_ID, "sv_req_b", "SV003", "Le Nguoi Mua B", "0903333333");

        availableSellItem = createItem(ITEM_ID, OWNER_ID, "Bàn học gấp gọn", "SELL", "AVAILABLE", new BigDecimal("150000"));
    }

    // =========================================================================
    // 1. KIỂM TRA GỬI YÊU CẦU GIAO DỊCH & RÀNG BUỘC UNIQUE (item_id, requester_id)
    // =========================================================================

    @Test
    void testSendRequestSuccessFromStudentAToStudentB() {
        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(Collections.emptyList());
        when(requestRepository.findByItemIdAndRequesterId(ITEM_ID, REQUESTER_A_ID)).thenReturn(Optional.empty());
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> {
            ExchangeRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ExchangeRequest created = service.createRequest(
                ITEM_ID, REQUESTER_A_ID, "SELL", null, "Mình ở tòa A phòng 101 muốn mua bàn này"
        );

        assertNotNull(created);
        assertEquals(ITEM_ID, created.getItemId());
        assertEquals(REQUESTER_A_ID, created.getRequesterId());
        assertEquals("PENDING", created.getStatus());
        assertEquals("SELL", created.getTransactionType());
        assertEquals("Mình ở tòa A phòng 101 muốn mua bàn này", created.getMessage());

        verify(notificationService, times(1)).createNotification(
                eq(OWNER_ID), eq("EXCHANGE"), anyString(), contains("Tran Nguoi Mua A"), eq("EXCHANGE_REQUEST"), eq(1L)
        );
    }

    @Test
    void testResendRequestAfterCancelledOrRejectedUpdatesExistingRecordId() {
        ExchangeRequest cancelledRecord = new ExchangeRequest();
        cancelledRecord.setId(77L);
        cancelledRecord.setItemId(ITEM_ID);
        cancelledRecord.setRequesterId(REQUESTER_A_ID);
        cancelledRecord.setStatus("CANCELLED");
        cancelledRecord.setRejectReason("Lý do cũ");
        cancelledRecord.setProcessedAt(LocalDateTime.now().minusDays(1));

        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(List.of(cancelledRecord));
        when(requestRepository.findByItemIdAndRequesterId(ITEM_ID, REQUESTER_A_ID)).thenReturn(Optional.of(cancelledRecord));
        when(requestRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(cancelledRecord));
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ExchangeRequest resent = service.createRequest(
                ITEM_ID, REQUESTER_A_ID, "SELL", null, "Gửi lại yêu cầu sau khi đã hủy"
        );

        // Phải cập nhật trực tiếp trên bản ghi cũ (id = 77L) chứ không tạo dòng mới gây lỗi UNIQUE KEY
        assertSame(cancelledRecord, resent);
        assertEquals(77L, resent.getId());
        assertEquals("PENDING", resent.getStatus());
        assertNull(resent.getProcessedAt());
        assertNull(resent.getRejectReason());
        assertEquals("Gửi lại yêu cầu sau khi đã hủy", resent.getMessage());
    }

    @Test
    void testSendRequestToOwnItemRejected() {
        when(studentRepository.findById(OWNER_ID)).thenReturn(Optional.of(ownerStudent));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.createRequest(ITEM_ID, OWNER_ID, "SELL", null, "Tự gửi cho mình")
        );
        assertTrue(ex.getMessage().contains("chính mình"));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testSendDuplicatePendingRequestRejected() {
        ExchangeRequest existingPending = new ExchangeRequest();
        existingPending.setId(5L);
        existingPending.setItemId(ITEM_ID);
        existingPending.setRequesterId(REQUESTER_A_ID);
        existingPending.setStatus("PENDING");

        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(List.of(existingPending));
        when(requestRepository.findByItemIdAndRequesterId(ITEM_ID, REQUESTER_A_ID))
                .thenReturn(Optional.of(existingPending));
        when(requestRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(existingPending));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.createRequest(ITEM_ID, REQUESTER_A_ID, "SELL", null, "Gửi trùng lần 2")
        );
        assertTrue(ex.getMessage().contains("đang chờ chủ tin xử lý"));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testSendRequestToCompletedOrClosedItemRejected() {
        ExchangeItem soldItem = createItem(ITEM_ID, OWNER_ID, "Đã bán", "SELL", "SOLD", new BigDecimal("100000"));
        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(soldItem));

        IllegalStateException exSold = assertThrows(IllegalStateException.class, () ->
                service.createRequest(ITEM_ID, REQUESTER_A_ID, "SELL", null, "Mua tin đã bán")
        );
        assertTrue(exSold.getMessage().contains("đã hoàn thành giao dịch"));

        ExchangeItem closedItem = createItem(ITEM_ID, OWNER_ID, "Đã ẩn", "SELL", "CLOSED", new BigDecimal("100000"));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(closedItem));

        IllegalStateException exClosed = assertThrows(IllegalStateException.class, () ->
                service.createRequest(ITEM_ID, REQUESTER_A_ID, "SELL", null, "Mua tin đã ẩn")
        );
        assertTrue(exClosed.getMessage().contains("đã bị ẩn"));
    }

    @Test
    void testSendExchangeRequestRequiresOfferedItemOrMessage() {
        ExchangeItem exchangeItem = createItem(ITEM_ID, OWNER_ID, "Ấm siêu tốc", "EXCHANGE", "AVAILABLE", null);
        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(exchangeItem));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.createRequest(ITEM_ID, REQUESTER_A_ID, "EXCHANGE", "   ", "   ")
        );
        assertTrue(ex.getMessage().contains("Trao đổi đồ"));
    }

    // =========================================================================
    // 2. KIỂM TRA CHỦ TIN XỬ LÝ YÊU CẦU & THỨ TỰ KHÓA (ITEM TRƯỚC -> REQUEST SAU)
    // =========================================================================

    @Test
    void testAcceptRequestUpdatesItemAndRejectsOtherPendingRequestsWithConsistentLockOrder() {
        ExchangeRequest req1 = new ExchangeRequest();
        req1.setId(1L);
        req1.setItemId(ITEM_ID);
        req1.setRequesterId(REQUESTER_A_ID);
        req1.setStatus("PENDING");

        ExchangeRequest req2 = new ExchangeRequest();
        req2.setId(2L);
        req2.setItemId(ITEM_ID);
        req2.setRequesterId(REQUESTER_B_ID);
        req2.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req1));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req1));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(List.of(req1, req2));
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ExchangeRequest accepted = service.acceptRequest(1L, OWNER_ID);

        // Kiểm tra thứ tự khóa nghiêm ngặt: Khóa ExchangeItem trước -> Khóa ExchangeRequest sau
        InOrder inOrder = inOrder(itemRepository, requestRepository);
        inOrder.verify(itemRepository).findByIdForUpdate(ITEM_ID);
        inOrder.verify(requestRepository).findByIdForUpdate(1L);

        // Yêu cầu 1 được chấp nhận
        assertEquals("ACCEPTED", accepted.getStatus());
        assertNotNull(accepted.getProcessedAt());

        // Tin đăng chuyển sang trạng thái PENDING (Đang giao dịch)
        assertEquals("PENDING", availableSellItem.getStatus());
        verify(itemRepository, times(1)).save(availableSellItem);

        // Yêu cầu 2 tự động chuyển sang REJECTED kèm lý do
        assertEquals("REJECTED", req2.getStatus());
        assertNotNull(req2.getProcessedAt());
        assertNotNull(req2.getRejectReason());
        assertTrue(req2.getRejectReason().contains("chấp nhận giao dịch với một sinh viên khác"));

        // Thông báo cho cả 2 sinh viên
        verify(notificationService, times(1)).createNotification(
                eq(REQUESTER_A_ID), eq("EXCHANGE"), contains("chấp nhận"), anyString(), eq("EXCHANGE_REQUEST"), eq(1L)
        );
        verify(notificationService, times(1)).createNotification(
                eq(REQUESTER_B_ID), eq("EXCHANGE"), contains("không được chọn"), anyString(), eq("EXCHANGE_REQUEST"), eq(2L)
        );
    }

    @Test
    void testAcceptRequestByNonOwnerOrRequesterRejected() {
        ExchangeRequest req1 = new ExchangeRequest();
        req1.setId(1L);
        req1.setItemId(ITEM_ID);
        req1.setRequesterId(REQUESTER_A_ID);
        req1.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req1));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req1));

        // Người gửi tự chấp nhận yêu cầu của mình -> bị từ chối
        assertThrows(SecurityException.class, () -> service.acceptRequest(1L, REQUESTER_A_ID));

        // Sinh viên khác không sở hữu tin chấp nhận -> bị từ chối
        assertThrows(SecurityException.class, () -> service.acceptRequest(1L, OUTSIDER_ID));
    }

    @Test
    void testRejectRequestByOwnerSuccess() {
        ExchangeRequest req1 = new ExchangeRequest();
        req1.setId(1L);
        req1.setItemId(ITEM_ID);
        req1.setRequesterId(REQUESTER_A_ID);
        req1.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req1));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req1));
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ExchangeRequest rejected = service.rejectRequest(1L, OWNER_ID, "Món đồ không phù hợp");

        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("Món đồ không phù hợp", rejected.getRejectReason());
        assertNotNull(rejected.getProcessedAt());
        verify(notificationService, times(1)).createNotification(
                eq(REQUESTER_A_ID), eq("EXCHANGE"), contains("từ chối"), contains("Món đồ không phù hợp"), eq("EXCHANGE_REQUEST"), eq(1L)
        );
    }

    // =========================================================================
    // 3. KIỂM TRA HỦY YÊU CẦU & XÁC NHẬN HOÀN THÀNH 2 PHÍA (IDEMPOTENT)
    // =========================================================================

    @Test
    void testCancelPendingRequestByRequesterSuccessAndRestoresItemIfPending() {
        ExchangeRequest req = new ExchangeRequest();
        req.setId(1L);
        req.setItemId(ITEM_ID);
        req.setRequesterId(REQUESTER_A_ID);
        req.setStatus("PENDING");

        availableSellItem.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req));
        when(requestRepository.findByItemId(ITEM_ID)).thenReturn(List.of(req));
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findById(REQUESTER_A_ID)).thenReturn(Optional.of(requesterA));

        ExchangeRequest cancelled = service.cancelRequest(1L, REQUESTER_A_ID);

        assertEquals("CANCELLED", cancelled.getStatus());
        assertNotNull(cancelled.getProcessedAt());
        assertEquals("AVAILABLE", availableSellItem.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(OWNER_ID), eq("EXCHANGE"), contains("hủy"), anyString(), eq("EXCHANGE_REQUEST"), eq(1L)
        );
    }

    @Test
    void testCancelRequestByOtherUserOrWhenAcceptedRejected() {
        ExchangeRequest req = new ExchangeRequest();
        req.setId(1L);
        req.setItemId(ITEM_ID);
        req.setRequesterId(REQUESTER_A_ID);
        req.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req));

        // Người khác hủy yêu cầu -> SecurityException
        assertThrows(SecurityException.class, () -> service.cancelRequest(1L, OWNER_ID));

        // Yêu cầu đã ACCEPTED -> không được hủy
        req.setStatus("ACCEPTED");
        assertThrows(IllegalStateException.class, () -> service.cancelRequest(1L, REQUESTER_A_ID));
    }

    @Test
    void testConfirmCompleteRequiresBothPartiesAndIsIdempotentOnRepeatedCalls() {
        ExchangeRequest req = new ExchangeRequest();
        req.setId(1L);
        req.setItemId(ITEM_ID);
        req.setRequesterId(REQUESTER_A_ID);
        req.setStatus("ACCEPTED");
        req.setOwnerConfirmed(false);
        req.setRequesterConfirmed(false);

        availableSellItem.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req));
        when(requestRepository.save(any(ExchangeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        // Bước 1: Người gửi xác nhận lần 1 -> chưa chuyển sang COMPLETED
        ExchangeRequest afterRequesterConfirm1 = service.confirmComplete(1L, REQUESTER_A_ID);
        assertTrue(afterRequesterConfirm1.isRequesterConfirmed());
        assertFalse(afterRequesterConfirm1.isOwnerConfirmed());
        assertEquals("ACCEPTED", afterRequesterConfirm1.getStatus());
        assertNull(afterRequesterConfirm1.getCompletedAt());
        assertEquals("PENDING", availableSellItem.getStatus());

        // Bước 2: Người gửi bấm xác nhận thêm lần 2 -> không báo lỗi, vẫn giữ ACCEPTED, không gửi thông báo thừa
        ExchangeRequest afterRequesterConfirm2 = service.confirmComplete(1L, REQUESTER_A_ID);
        assertEquals("ACCEPTED", afterRequesterConfirm2.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(OWNER_ID), eq("EXCHANGE"), contains("xác nhận"), anyString(), eq("EXCHANGE_REQUEST"), eq(1L)
        );

        // Bước 3: Chủ tin xác nhận -> đủ 2 bên -> chuyển sang COMPLETED đúng 1 lần và cập nhật item sang SOLD
        ExchangeRequest afterOwnerConfirm = service.confirmComplete(1L, OWNER_ID);
        assertTrue(afterOwnerConfirm.isOwnerConfirmed());
        assertTrue(afterOwnerConfirm.isRequesterConfirmed());
        assertEquals("COMPLETED", afterOwnerConfirm.getStatus());
        LocalDateTime firstCompletedAt = afterOwnerConfirm.getCompletedAt();
        assertNotNull(firstCompletedAt);
        assertEquals("SOLD", availableSellItem.getStatus());

        // Bước 4: Gọi lại confirmComplete sau khi đã COMPLETED -> trả về an toàn, không đổi completedAt, không lưu lại
        ExchangeRequest afterCompletedRetry = service.confirmComplete(1L, OWNER_ID);
        assertEquals("COMPLETED", afterCompletedRetry.getStatus());
        assertEquals(firstCompletedAt, afterCompletedRetry.getCompletedAt());
        verify(itemRepository, times(1)).save(availableSellItem);
    }

    @Test
    void testConfirmCompleteWhenNotAcceptedRejected() {
        ExchangeRequest req = new ExchangeRequest();
        req.setId(1L);
        req.setItemId(ITEM_ID);
        req.setRequesterId(REQUESTER_A_ID);
        req.setStatus("PENDING");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(itemRepository.findByIdForUpdate(ITEM_ID)).thenReturn(Optional.of(availableSellItem));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req));

        assertThrows(IllegalStateException.class, () -> service.confirmComplete(1L, OWNER_ID));

        req.setStatus("REJECTED");
        assertThrows(IllegalStateException.class, () -> service.confirmComplete(1L, OWNER_ID));
    }

    @Test
    void testGetHistoryViewsDistinguishesRolesCorrectly() {
        ExchangeRequest completedReq = new ExchangeRequest();
        completedReq.setId(10L);
        completedReq.setItemId(ITEM_ID);
        completedReq.setRequesterId(REQUESTER_A_ID);
        completedReq.setTransactionType("SELL");
        completedReq.setStatus("COMPLETED");

        when(requestRepository.findHistoryByStudentIdAndStatuses(eq(OWNER_ID), anyCollection()))
                .thenReturn(List.of(completedReq));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(availableSellItem));
        when(studentRepository.findAllById(anyCollection())).thenReturn(List.of(ownerStudent, requesterA));

        Page<ExchangeRequestView> ownerHistory = service.getHistoryViews(OWNER_ID, null, null, 0, 10);
        assertEquals(1, ownerHistory.getTotalElements());
        ExchangeRequestView viewForOwner = ownerHistory.getContent().get(0);
        assertTrue(viewForOwner.isCurrentUserIsOwner());
        assertEquals("Chủ tin đăng", viewForOwner.getRoleLabel());
        assertEquals("Tran Nguoi Mua A", viewForOwner.getCounterpartyName());
    }

    private Student createStudent(Long id, String username, String code, String fullName, String phone) {
        Student s = new Student(username, "hashed", code, fullName);
        s.setPhone(phone);
        try {
            Field idField = com.dormitory.entity.User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(s, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    private ExchangeItem createItem(Long id, Long ownerId, String title, String type, String status, BigDecimal price) {
        ExchangeItem item = new ExchangeItem();
        try {
            Field idField = ExchangeItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        item.setOwnerId(ownerId);
        item.setTitle(title);
        item.setCategory("Đồ gia dụng - Phòng ở");
        item.setExchangeType(type);
        item.setItemCondition("USED");
        item.setStatus(status);
        item.setPrice(price);
        return item;
    }
}
