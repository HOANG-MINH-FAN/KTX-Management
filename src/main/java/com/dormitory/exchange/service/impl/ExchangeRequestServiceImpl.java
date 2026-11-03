package com.dormitory.exchange.service.impl;

import com.dormitory.entity.Student;
import com.dormitory.exchange.dto.ExchangeRequestView;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.service.ExchangeRequestService;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExchangeRequestServiceImpl implements ExchangeRequestService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRequestServiceImpl.class);

    public static final List<String> VALID_STATUSES = List.of(
            ExchangeRequest.STATUS_PENDING,
            ExchangeRequest.STATUS_ACCEPTED,
            ExchangeRequest.STATUS_REJECTED,
            ExchangeRequest.STATUS_CANCELLED,
            ExchangeRequest.STATUS_COMPLETED
    );

    public static final List<String> HISTORY_STATUSES = List.of(
            ExchangeRequest.STATUS_ACCEPTED,
            ExchangeRequest.STATUS_REJECTED,
            ExchangeRequest.STATUS_CANCELLED,
            ExchangeRequest.STATUS_COMPLETED
    );

    private final ExchangeRequestRepository repository;
    private final ExchangeItemRepository itemRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public ExchangeRequestServiceImpl(ExchangeRequestRepository repository,
                                      ExchangeItemRepository itemRepository,
                                      StudentRepository studentRepository,
                                      NotificationService notificationService) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getByItemId(Long itemId) {
        return repository.findByItemId(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getByRequesterId(Long requesterId) {
        return repository.findByRequesterId(requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRequest> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public ExchangeRequest save(ExchangeRequest request) {
        return repository.save(request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public ExchangeRequest createRequest(Long itemId,
                                         Long requesterStudentId,
                                         String transactionType,
                                         String offeredItem,
                                         String message) {
        if (itemId == null) {
            throw new IllegalArgumentException("Mã tin đăng không hợp lệ.");
        }
        if (requesterStudentId == null) {
            throw new SecurityException("Bạn cần đăng nhập bằng tài khoản sinh viên để gửi yêu cầu.");
        }

        Student requester = studentRepository.findById(requesterStudentId)
                .orElseThrow(() -> new SecurityException("Chỉ sinh viên hợp lệ mới có quyền gửi yêu cầu giao dịch."));

        // Khóa theo thứ tự chuẩn: ExchangeItem trước -> ExchangeRequest sau
        ExchangeItem item = findItemWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Tin đăng không tồn tại hoặc đã bị xóa."));

        if (item.getOwnerId().equals(requesterStudentId)) {
            throw new IllegalArgumentException("Bạn không thể gửi yêu cầu giao dịch đối với tin đăng của chính mình.");
        }

        String itemStatus = item.getStatus() != null ? item.getStatus().toUpperCase() : "";
        if ("CLOSED".equals(itemStatus)) {
            throw new IllegalStateException("Tin đăng này đã bị ẩn hoặc không còn nhận yêu cầu.");
        }
        if ("SOLD".equals(itemStatus) || "EXCHANGED".equals(itemStatus)) {
            throw new IllegalStateException("Tin đăng này đã hoàn thành giao dịch, không thể gửi thêm yêu cầu.");
        }
        if ("PENDING".equals(itemStatus)) {
            throw new IllegalStateException("Tin đăng này đang trong quá trình giao dịch với sinh viên khác.");
        }
        if (!"AVAILABLE".equals(itemStatus)) {
            throw new IllegalStateException("Tin đăng hiện không ở trạng thái sẵn sàng giao dịch.");
        }

        // Kiểm tra xem tin đã có yêu cầu được chấp nhận hoặc hoàn thành chưa
        List<ExchangeRequest> itemRequests = repository.findByItemId(itemId);
        if (itemRequests != null) {
            boolean hasActiveOrDone = itemRequests.stream().anyMatch(r ->
                    ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus())
                            || ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(r.getStatus())
            );
            if (hasActiveOrDone) {
                throw new IllegalStateException("Tin đăng này đã có giao dịch được chấp nhận hoặc hoàn thành.");
            }
        }

        // Kiểm tra hình thức giao dịch có phù hợp với tin đăng hay không
        String expectedType = item.getExchangeType() != null ? item.getExchangeType().trim().toUpperCase() : "SELL";
        if (transactionType != null && !transactionType.trim().isEmpty()) {
            String requestedType = transactionType.trim().toUpperCase();
            if (!expectedType.equals(requestedType)) {
                throw new IllegalArgumentException("Hình thức giao dịch không khớp với tin đăng (" +
                        ExchangeRequestView.resolveTransactionTypeLabel(expectedType) + ").");
            }
        }

        String cleanOfferedItem = offeredItem != null && !offeredItem.trim().isEmpty() ? offeredItem.trim() : null;
        String cleanMessage = message != null && !message.trim().isEmpty() ? message.trim() : null;

        if ("EXCHANGE".equals(expectedType)) {
            if (cleanOfferedItem == null && cleanMessage == null) {
                throw new IllegalArgumentException("Đối với hình thức Trao đổi đồ, vui lòng nhập món đồ bạn muốn dùng để trao đổi hoặc lời nhắn cụ thể.");
            }
            if (cleanOfferedItem != null && cleanOfferedItem.length() > 500) {
                throw new IllegalArgumentException("Tên món đồ đề nghị trao đổi không được vượt quá 500 ký tự.");
            }
        } else {
            cleanOfferedItem = null;
        }

        if (cleanMessage != null && cleanMessage.length() > 2000) {
            throw new IllegalArgumentException("Lời nhắn không được vượt quá 2000 ký tự.");
        }

        // Kiểm tra yêu cầu trùng lặp của cùng người gửi và cùng tin đăng.
        // Nếu bản ghi cũ có trạng thái CANCELLED hoặc REJECTED, CẬP NHẬT lại chính bản ghi đó (giữ nguyên id)
        // để không vi phạm UNIQUE KEY uk_exchange_request_item_user (item_id, requester_id).
        Optional<ExchangeRequest> existingOpt = repository.findByItemIdAndRequesterId(itemId, requesterStudentId);
        ExchangeRequest request;
        if (existingOpt.isPresent()) {
            ExchangeRequest existing = findRequestWithLock(existingOpt.get().getId()).orElse(existingOpt.get());
            String existingStatus = existing.getStatus();
            if (ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(existingStatus)) {
                throw new IllegalStateException("Bạn đã gửi yêu cầu cho tin đăng này và yêu cầu đang chờ chủ tin xử lý.");
            }
            if (ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(existingStatus)
                    || ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(existingStatus)) {
                throw new IllegalStateException("Yêu cầu giao dịch của bạn đối với tin đăng này đã được chấp nhận hoặc hoàn thành.");
            }
            request = existing;
            request.setStatus(ExchangeRequest.STATUS_PENDING);
            request.setCreatedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            request.setProcessedAt(null);
            request.setCompletedAt(null);
            request.setRejectReason(null);
            request.setOwnerConfirmed(false);
            request.setRequesterConfirmed(false);
        } else {
            request = new ExchangeRequest();
            request.setItemId(itemId);
            request.setRequesterId(requesterStudentId);
            request.setStatus(ExchangeRequest.STATUS_PENDING);
            request.setCreatedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            request.setOwnerConfirmed(false);
            request.setRequesterConfirmed(false);
        }

        request.setTransactionType(expectedType);
        request.setOfferedItem(cleanOfferedItem);
        request.setMessage(cleanMessage);

        ExchangeRequest saved = repository.save(request);

        String typeLabel = ExchangeRequestView.resolveTransactionTypeLabel(expectedType).toLowerCase();
        sendNotificationSafely(
                item.getOwnerId(),
                "EXCHANGE",
                "Có yêu cầu giao dịch mới",
                "Sinh viên " + requester.getFullName() + " vừa gửi yêu cầu " + typeLabel
                        + " cho tin đăng \"" + item.getTitle() + "\" của bạn.",
                "EXCHANGE_REQUEST",
                saved.getId()
        );

        return saved;
    }

    @Override
    @Transactional
    public ExchangeRequest acceptRequest(Long requestId, Long ownerStudentId) {
        if (requestId == null || ownerStudentId == null) {
            throw new IllegalArgumentException("Thông tin xử lý yêu cầu không hợp lệ.");
        }

        // Bước 1: Đọc thông tin yêu cầu để xác định itemId
        ExchangeRequest initialRequest = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        // Bước 2: Khóa theo thứ tự chuẩn: Khóa ExchangeItem trước -> Khóa ExchangeRequest sau (tránh deadlock)
        ExchangeItem item = findItemWithLock(initialRequest.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng của yêu cầu này."));

        ExchangeRequest request = findRequestWithLock(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        if (!item.getOwnerId().equals(ownerStudentId)) {
            throw new SecurityException("Chỉ chủ sở hữu tin đăng mới được phép chấp nhận yêu cầu này.");
        }

        if (!ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó và không còn ở trạng thái chờ xử lý.");
        }

        if (!"AVAILABLE".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalStateException("Tin đăng không còn ở trạng thái sẵn sàng (AVAILABLE) để chấp nhận yêu cầu.");
        }

        List<ExchangeRequest> allRequestsForItem = repository.findByItemId(item.getId());
        if (allRequestsForItem != null) {
            boolean alreadyAccepted = allRequestsForItem.stream()
                    .anyMatch(r -> !r.getId().equals(request.getId()) &&
                            (ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus())
                                    || ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(r.getStatus())));
            if (alreadyAccepted) {
                throw new IllegalStateException("Tin đăng này đã có một yêu cầu khác được chấp nhận hoặc hoàn thành.");
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Cập nhật yêu cầu được chọn sang ACCEPTED
        request.setStatus(ExchangeRequest.STATUS_ACCEPTED);
        request.setProcessedAt(now);
        request.setRejectReason(null);
        request.setOwnerConfirmed(false);
        request.setRequesterConfirmed(false);
        ExchangeRequest savedRequest = repository.save(request);

        // 2. Đánh dấu tin đăng sang trạng thái PENDING (Đang giao dịch) để không nhận yêu cầu mới
        item.setStatus("PENDING");
        itemRepository.save(item);

        // 3. Tự động từ chối tất cả các yêu cầu khác đang PENDING của cùng tin đăng
        String autoRejectReason = "Tin đăng đã được chủ tin chấp nhận giao dịch với một sinh viên khác.";
        if (allRequestsForItem != null) {
            for (ExchangeRequest other : allRequestsForItem) {
                if (!other.getId().equals(request.getId())
                        && ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(other.getStatus())) {
                    other.setStatus(ExchangeRequest.STATUS_REJECTED);
                    other.setProcessedAt(now);
                    other.setRejectReason(autoRejectReason);
                    repository.save(other);

                    sendNotificationSafely(
                            other.getRequesterId(),
                            "EXCHANGE",
                            "Yêu cầu giao dịch không được chọn",
                            "Yêu cầu của bạn đối với món đồ \"" + item.getTitle()
                                    + "\" đã bị từ chối tự động vì chủ tin đã chấp nhận giao dịch với sinh viên khác.",
                            "EXCHANGE_REQUEST",
                            other.getId()
                    );
                }
            }
        }

        // 4. Thông báo cho sinh viên được chấp nhận
        sendNotificationSafely(
                request.getRequesterId(),
                "EXCHANGE",
                "Yêu cầu giao dịch đã được chấp nhận",
                "Chủ tin đã chấp nhận yêu cầu giao dịch của bạn đối với món đồ \"" + item.getTitle()
                        + "\". Vui lòng liên hệ chủ tin qua số điện thoại để nhận/trao đổi đồ và xác nhận hoàn thành.",
                "EXCHANGE_REQUEST",
                savedRequest.getId()
        );

        return savedRequest;
    }

    @Override
    @Transactional
    public ExchangeRequest rejectRequest(Long requestId, Long ownerStudentId, String rejectReason) {
        if (requestId == null || ownerStudentId == null) {
            throw new IllegalArgumentException("Thông tin xử lý yêu cầu không hợp lệ.");
        }

        ExchangeRequest initialRequest = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        // Khóa theo thứ tự chuẩn: ExchangeItem trước -> ExchangeRequest sau
        ExchangeItem item = findItemWithLock(initialRequest.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng của yêu cầu này."));

        ExchangeRequest request = findRequestWithLock(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        if (!item.getOwnerId().equals(ownerStudentId)) {
            throw new SecurityException("Chỉ chủ sở hữu tin đăng mới được phép từ chối yêu cầu này.");
        }

        if (!ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó, không thể từ chối lại.");
        }

        String finalReason = (rejectReason != null && !rejectReason.trim().isEmpty())
                ? rejectReason.trim()
                : "Chủ tin đã từ chối yêu cầu giao dịch.";

        request.setStatus(ExchangeRequest.STATUS_REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setRejectReason(finalReason);

        ExchangeRequest saved = repository.save(request);

        // Bảo đảm nếu tin đăng đang ở trạng thái PENDING nhưng không còn yêu cầu nào ACCEPTED/COMPLETED thì khôi phục về AVAILABLE
        restoreItemToAvailableIfNoActiveTransaction(item, saved.getId());

        sendNotificationSafely(
                request.getRequesterId(),
                "EXCHANGE",
                "Yêu cầu giao dịch bị từ chối",
                "Chủ tin đã từ chối yêu cầu của bạn đối với món đồ \"" + item.getTitle()
                        + "\". Lý do: " + finalReason,
                "EXCHANGE_REQUEST",
                saved.getId()
        );

        return saved;
    }

    @Override
    @Transactional
    public ExchangeRequest cancelRequest(Long requestId, Long requesterStudentId) {
        if (requestId == null || requesterStudentId == null) {
            throw new IllegalArgumentException("Thông tin hủy yêu cầu không hợp lệ.");
        }

        ExchangeRequest initialRequest = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        // Khóa theo thứ tự chuẩn: ExchangeItem (nếu còn tồn tại) trước -> ExchangeRequest sau
        ExchangeItem item = findItemWithLock(initialRequest.getItemId()).orElse(null);

        ExchangeRequest request = findRequestWithLock(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        if (!request.getRequesterId().equals(requesterStudentId)) {
            throw new SecurityException("Bạn chỉ có thể hủy yêu cầu giao dịch do chính mình gửi.");
        }

        if (!ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu khi đang ở trạng thái chờ xử lý (PENDING).");
        }

        request.setStatus(ExchangeRequest.STATUS_CANCELLED);
        request.setProcessedAt(LocalDateTime.now());

        ExchangeRequest saved = repository.save(request);

        if (item != null) {
            restoreItemToAvailableIfNoActiveTransaction(item, saved.getId());

            String requesterName = studentRepository.findById(requesterStudentId)
                    .map(Student::getFullName)
                    .orElse("Sinh viên");
            sendNotificationSafely(
                    item.getOwnerId(),
                    "EXCHANGE",
                    "Yêu cầu giao dịch đã bị hủy",
                    requesterName + " đã hủy yêu cầu giao dịch đối với tin đăng \"" + item.getTitle() + "\" của bạn.",
                    "EXCHANGE_REQUEST",
                    saved.getId()
            );
        }

        return saved;
    }

    @Override
    @Transactional
    public ExchangeRequest confirmComplete(Long requestId, Long currentStudentId) {
        if (requestId == null || currentStudentId == null) {
            throw new IllegalArgumentException("Thông tin xác nhận hoàn thành không hợp lệ.");
        }

        ExchangeRequest initialRequest = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        // Khóa theo thứ tự chuẩn: ExchangeItem trước -> ExchangeRequest sau
        ExchangeItem item = findItemWithLock(initialRequest.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin đăng của giao dịch này."));

        ExchangeRequest request = findRequestWithLock(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giao dịch."));

        boolean isOwner = item.getOwnerId().equals(currentStudentId);
        boolean isRequester = request.getRequesterId().equals(currentStudentId);

        if (!isOwner && !isRequester) {
            throw new SecurityException("Bạn không có quyền xác nhận hoàn thành giao dịch này.");
        }

        // Idempotent: Nếu giao dịch đã COMPLETED trước đó, trả về ngay mà không gây lỗi và không tạo lại thông báo
        if (ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(request.getStatus())) {
            return request;
        }

        if (!ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Chỉ yêu cầu đã được chấp nhận (ACCEPTED) mới có thể xác nhận hoàn thành.");
        }

        // Idempotent: Nếu bên hiện tại đã xác nhận rồi (và đang chờ bên kia), trả về ngay không báo lỗi
        if (isOwner) {
            if (request.isOwnerConfirmed()) {
                return request;
            }
            request.setOwnerConfirmed(true);
        } else {
            if (request.isRequesterConfirmed()) {
                return request;
            }
            request.setRequesterConfirmed(true);
        }

        // Chỉ chuyển sang COMPLETED đúng một lần khi cả hai bên (chủ tin và người gửi) đều đã xác nhận
        if (request.isOwnerConfirmed() && request.isRequesterConfirmed()) {
            LocalDateTime now = LocalDateTime.now();
            request.setStatus(ExchangeRequest.STATUS_COMPLETED);
            request.setCompletedAt(now);

            if ("SELL".equalsIgnoreCase(item.getExchangeType())) {
                item.setStatus("SOLD");
            } else {
                item.setStatus("EXCHANGED");
            }
            itemRepository.save(item);

            ExchangeRequest saved = repository.save(request);

            sendNotificationSafely(
                    item.getOwnerId(),
                    "EXCHANGE",
                    "Giao dịch hoàn thành",
                    "Giao dịch cho món đồ \"" + item.getTitle() + "\" đã được cả hai bên xác nhận hoàn thành.",
                    "EXCHANGE_REQUEST",
                    saved.getId()
            );

            sendNotificationSafely(
                    request.getRequesterId(),
                    "EXCHANGE",
                    "Giao dịch hoàn thành",
                    "Giao dịch cho món đồ \"" + item.getTitle() + "\" đã được cả hai bên xác nhận hoàn thành.",
                    "EXCHANGE_REQUEST",
                    saved.getId()
            );

            return saved;
        } else {
            ExchangeRequest saved = repository.save(request);

            if (isOwner) {
                sendNotificationSafely(
                        request.getRequesterId(),
                        "EXCHANGE",
                        "Chủ tin đã xác nhận hoàn thành giao dịch",
                        "Chủ tin của món đồ \"" + item.getTitle()
                                + "\" đã xác nhận hoàn tất giao dịch. Vui lòng vào mục Yêu cầu đã gửi hoặc Lịch sử giao dịch để xác nhận hoàn thành.",
                        "EXCHANGE_REQUEST",
                        saved.getId()
                );
            } else {
                sendNotificationSafely(
                        item.getOwnerId(),
                        "EXCHANGE",
                        "Người nhận đã xác nhận hoàn thành giao dịch",
                        "Người gửi yêu cầu cho món đồ \"" + item.getTitle()
                                + "\" đã xác nhận nhận đồ. Vui lòng vào mục Yêu cầu nhận được hoặc Lịch sử giao dịch để xác nhận hoàn thành.",
                        "EXCHANGE_REQUEST",
                        saved.getId()
                );
            }

            return saved;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequestView> getSentRequestViews(Long requesterStudentId, String statusFilter) {
        if (requesterStudentId == null) {
            return Collections.emptyList();
        }

        String normalizedStatus = normalizeStatusFilter(statusFilter);
        List<ExchangeRequest> requests;
        if (normalizedStatus != null) {
            requests = repository.findByRequesterIdAndStatusOrderByCreatedAtDesc(requesterStudentId, normalizedStatus);
        } else {
            requests = repository.findByRequesterIdOrderByCreatedAtDesc(requesterStudentId);
        }

        return mapToViews(requests, requesterStudentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequestView> getReceivedRequestViews(Long ownerStudentId, String statusFilter, Long itemIdFilter) {
        if (ownerStudentId == null) {
            return Collections.emptyList();
        }

        List<ExchangeItem> myItems = itemRepository.findByOwnerId(ownerStudentId);
        if (myItems == null || myItems.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> myItemIds = myItems.stream()
                .map(ExchangeItem::getId)
                .filter(id -> itemIdFilter == null || id.equals(itemIdFilter))
                .collect(Collectors.toSet());

        if (myItemIds.isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedStatus = normalizeStatusFilter(statusFilter);
        List<ExchangeRequest> requests;
        if (normalizedStatus != null) {
            requests = repository.findByItemIdInAndStatusOrderByCreatedAtDesc(myItemIds, normalizedStatus);
        } else {
            requests = repository.findByItemIdInOrderByCreatedAtDesc(myItemIds);
        }

        return mapToViews(requests, ownerStudentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExchangeRequestView> getHistoryViews(Long studentId, String statusFilter, String roleFilter, int page, int size) {
        int safeSize = size > 0 ? size : 10;
        int safePage = Math.max(page, 0);

        if (studentId == null) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, safeSize), 0);
        }

        String normalizedStatus = normalizeStatusFilter(statusFilter);
        List<String> targetStatuses;
        if (normalizedStatus != null && HISTORY_STATUSES.contains(normalizedStatus)) {
            targetStatuses = List.of(normalizedStatus);
        } else {
            targetStatuses = HISTORY_STATUSES;
        }

        List<ExchangeRequest> rawHistory = repository.findHistoryByStudentIdAndStatuses(studentId, targetStatuses);
        List<ExchangeRequestView> allViews = mapToViews(rawHistory, studentId);

        if (roleFilter != null && !roleFilter.trim().isEmpty()) {
            String role = roleFilter.trim().toUpperCase();
            if ("OWNER".equals(role)) {
                allViews = allViews.stream().filter(ExchangeRequestView::isCurrentUserIsOwner).collect(Collectors.toList());
            } else if ("REQUESTER".equals(role)) {
                allViews = allViews.stream().filter(ExchangeRequestView::isCurrentUserIsRequester).collect(Collectors.toList());
            }
        }

        int total = allViews.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / safeSize));
        int currentPage = Math.min(safePage, pageCount - 1);
        Pageable pageable = PageRequest.of(currentPage, safeSize);
        int from = Math.min(currentPage * safeSize, total);
        int to = Math.min(from + safeSize, total);

        return new PageImpl<>(allViews.subList(from, to), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRequest> getRequestByItemAndRequester(Long itemId, Long requesterStudentId) {
        if (itemId == null || requesterStudentId == null) {
            return Optional.empty();
        }
        return repository.findByItemIdAndRequesterId(itemId, requesterStudentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReceivedRequests(Long ownerStudentId) {
        if (ownerStudentId == null) {
            return 0L;
        }
        List<ExchangeItem> myItems = itemRepository.findByOwnerId(ownerStudentId);
        if (myItems == null || myItems.isEmpty()) {
            return 0L;
        }
        Set<Long> itemIds = myItems.stream().map(ExchangeItem::getId).collect(Collectors.toSet());
        return repository.findByItemIdInAndStatusOrderByCreatedAtDesc(itemIds, ExchangeRequest.STATUS_PENDING).size();
    }

    /**
     * Nếu tin đăng đang ở trạng thái PENDING nhưng không còn yêu cầu nào ACCEPTED hoặc COMPLETED,
     * khôi phục trạng thái tin đăng về AVAILABLE để có thể tiếp tục nhận yêu cầu.
     */
    private void restoreItemToAvailableIfNoActiveTransaction(ExchangeItem item, Long excludedRequestId) {
        if (item == null || !"PENDING".equalsIgnoreCase(item.getStatus())) {
            return;
        }
        List<ExchangeRequest> requests = repository.findByItemId(item.getId());
        boolean hasOtherActive = requests != null && requests.stream()
                .anyMatch(r -> !Objects.equals(r.getId(), excludedRequestId)
                        && (ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus())
                            || ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(r.getStatus())));
        if (!hasOtherActive) {
            item.setStatus("AVAILABLE");
            itemRepository.save(item);
        }
    }

    private Optional<ExchangeItem> findItemWithLock(Long itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        Optional<ExchangeItem> locked = itemRepository.findByIdForUpdate(itemId);
        return locked.isPresent() ? locked : itemRepository.findById(itemId);
    }

    private Optional<ExchangeRequest> findRequestWithLock(Long requestId) {
        if (requestId == null) {
            return Optional.empty();
        }
        Optional<ExchangeRequest> locked = repository.findByIdForUpdate(requestId);
        return locked.isPresent() ? locked : repository.findById(requestId);
    }

    private String normalizeStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            return null;
        }
        String upper = statusFilter.trim().toUpperCase();
        return VALID_STATUSES.contains(upper) ? upper : null;
    }

    private List<ExchangeRequestView> mapToViews(List<ExchangeRequest> requests, Long currentStudentId) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> itemIds = requests.stream()
                .map(ExchangeRequest::getItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ExchangeItem> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(ExchangeItem::getId, i -> i));

        Set<Long> studentIds = new HashSet<>();
        for (ExchangeRequest r : requests) {
            if (r.getRequesterId() != null) {
                studentIds.add(r.getRequesterId());
            }
            ExchangeItem item = itemMap.get(r.getItemId());
            if (item != null && item.getOwnerId() != null) {
                studentIds.add(item.getOwnerId());
            }
        }

        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<ExchangeRequestView> views = new ArrayList<>(requests.size());
        for (ExchangeRequest r : requests) {
            ExchangeItem item = itemMap.get(r.getItemId());
            Student owner = (item != null && item.getOwnerId() != null) ? studentMap.get(item.getOwnerId()) : null;
            Student requester = r.getRequesterId() != null ? studentMap.get(r.getRequesterId()) : null;
            views.add(new ExchangeRequestView(r, item, owner, requester, currentStudentId));
        }
        return views;
    }

    private void sendNotificationSafely(Long userId,
                                        String type,
                                        String title,
                                        String message,
                                        String relatedType,
                                        Long relatedId) {
        if (userId == null || notificationService == null) {
            return;
        }
        try {
            notificationService.createNotification(userId, type, title, message, relatedType, relatedId);
        } catch (Exception e) {
            log.warn("Không thể gửi thông báo cho userId={}: {}", userId, e.getMessage());
        }
    }
}
