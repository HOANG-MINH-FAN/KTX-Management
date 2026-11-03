package com.dormitory.exchange.service;

import com.dormitory.exchange.dto.ExchangeRequestView;
import com.dormitory.exchange.entity.ExchangeRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ExchangeRequestService {

    List<ExchangeRequest> getAll();

    List<ExchangeRequest> getByItemId(Long itemId);

    List<ExchangeRequest> getByRequesterId(Long requesterId);

    Optional<ExchangeRequest> getById(Long id);

    ExchangeRequest save(ExchangeRequest request);

    void delete(Long id);

    /**
     * Sinh viên gửi yêu cầu giao dịch (Mua / Nhận đồ miễn phí / Trao đổi) đối với một tin đăng.
     */
    ExchangeRequest createRequest(Long itemId,
                                  Long requesterStudentId,
                                  String transactionType,
                                  String offeredItem,
                                  String message);

    /**
     * Chủ tin chấp nhận một yêu cầu đang chờ xử lý.
     * Tự động chuyển trạng thái tin đăng sang PENDING và từ chối các yêu cầu đang chờ khác của cùng tin đăng.
     */
    ExchangeRequest acceptRequest(Long requestId, Long ownerStudentId);

    /**
     * Chủ tin từ chối một yêu cầu đang chờ xử lý.
     */
    ExchangeRequest rejectRequest(Long requestId, Long ownerStudentId, String rejectReason);

    /**
     * Người gửi hủy yêu cầu của chính mình khi yêu cầu còn ở trạng thái PENDING.
     */
    ExchangeRequest cancelRequest(Long requestId, Long requesterStudentId);

    /**
     * Xác nhận hoàn thành giao dịch từ phía chủ tin hoặc người gửi.
     * Khi cả hai bên đều đã xác nhận, yêu cầu chuyển sang COMPLETED và tin đăng chuyển sang SOLD/EXCHANGED.
     */
    ExchangeRequest confirmComplete(Long requestId, Long currentStudentId);

    /**
     * Danh sách yêu cầu đã gửi của sinh viên hiện tại.
     */
    List<ExchangeRequestView> getSentRequestViews(Long requesterStudentId, String statusFilter);

    /**
     * Danh sách yêu cầu nhận được đối với các tin đăng thuộc sở hữu của sinh viên hiện tại.
     */
    List<ExchangeRequestView> getReceivedRequestViews(Long ownerStudentId, String statusFilter, Long itemIdFilter);

    /**
     * Lịch sử giao dịch (ACCEPTED, REJECTED, CANCELLED, COMPLETED) mà sinh viên hiện tại tham gia.
     */
    Page<ExchangeRequestView> getHistoryViews(Long studentId, String statusFilter, String roleFilter, int page, int size);

    /**
     * Tìm yêu cầu hiện có của sinh viên đối với một tin đăng cụ thể.
     */
    Optional<ExchangeRequest> getRequestByItemAndRequester(Long itemId, Long requesterStudentId);

    /**
     * Đếm số yêu cầu đang chờ xử lý gửi đến các tin của chủ tin.
     */
    long countPendingReceivedRequests(Long ownerStudentId);
}
