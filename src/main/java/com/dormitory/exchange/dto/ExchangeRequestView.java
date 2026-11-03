package com.dormitory.exchange.dto;

import com.dormitory.entity.Student;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin yêu cầu và giao dịch trao đổi đồ trên giao diện,
 * tách biệt khỏi Entity gốc và tránh lộ thông tin nhạy cảm của tài khoản.
 */
public class ExchangeRequestView {

    private final Long id;
    private final Long itemId;
    private final boolean itemExists;
    private final boolean itemViewable;
    private final String itemTitle;
    private final String itemImagePath;
    private final String itemStatus;
    private final BigDecimal itemPrice;
    private final String itemDesiredItem;

    private final String transactionType;
    private final String transactionTypeLabel;
    private final String offeredItem;
    private final String message;
    private final String rejectReason;

    private final String status;
    private final String statusLabel;

    private final Long ownerId;
    private final String ownerName;
    private final String ownerStudentCode;
    private final String ownerPhone;

    private final Long requesterId;
    private final String requesterName;
    private final String requesterStudentCode;
    private final String requesterPhone;

    private final boolean currentUserIsOwner;
    private final boolean currentUserIsRequester;
    private final String roleLabel;
    private final String counterpartyName;
    private final String counterpartyStudentCode;
    private final String counterpartyPhone;

    private final boolean ownerConfirmed;
    private final boolean requesterConfirmed;

    private final boolean canCancel;
    private final boolean canAccept;
    private final boolean canReject;
    private final boolean canConfirmComplete;

    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime updatedAt;

    public ExchangeRequestView(ExchangeRequest request,
                               ExchangeItem item,
                               Student owner,
                               Student requester,
                               Long currentStudentId) {
        this.id = request.getId();
        this.itemId = request.getItemId();
        this.itemExists = item != null;
        this.itemStatus = item != null ? item.getStatus() : "DELETED";
        this.itemViewable = item != null && "AVAILABLE".equalsIgnoreCase(item.getStatus());
        this.itemTitle = item != null ? item.getTitle() : "Tin đăng không còn tồn tại (#" + request.getItemId() + ")";
        this.itemImagePath = item != null ? item.getImagePath() : null;
        this.itemPrice = item != null ? item.getPrice() : null;
        this.itemDesiredItem = item != null ? item.getDesiredItem() : null;

        String effectiveType = request.getTransactionType() != null && !request.getTransactionType().isBlank()
                ? request.getTransactionType()
                : (item != null ? item.getExchangeType() : "EXCHANGE");
        this.transactionType = effectiveType;
        this.transactionTypeLabel = resolveTransactionTypeLabel(effectiveType);

        this.offeredItem = request.getOfferedItem();
        this.message = request.getMessage();
        this.rejectReason = request.getRejectReason();

        this.status = request.getStatus();
        this.statusLabel = resolveStatusLabel(request.getStatus());

        this.ownerId = item != null ? item.getOwnerId() : (owner != null ? owner.getId() : null);
        this.ownerName = owner != null && owner.getFullName() != null ? owner.getFullName() : "Sinh viên KTX";
        this.ownerStudentCode = owner != null ? owner.getStudentCode() : null;
        this.ownerPhone = owner != null ? owner.getPhone() : null;

        this.requesterId = request.getRequesterId();
        this.requesterName = requester != null && requester.getFullName() != null ? requester.getFullName() : "Sinh viên KTX";
        this.requesterStudentCode = requester != null ? requester.getStudentCode() : null;
        this.requesterPhone = requester != null ? requester.getPhone() : null;

        this.currentUserIsOwner = currentStudentId != null && this.ownerId != null && currentStudentId.equals(this.ownerId);
        this.currentUserIsRequester = currentStudentId != null && this.requesterId != null && currentStudentId.equals(this.requesterId);

        if (this.currentUserIsOwner) {
            this.roleLabel = "Chủ tin đăng";
            this.counterpartyName = this.requesterName;
            this.counterpartyStudentCode = this.requesterStudentCode;
            this.counterpartyPhone = this.requesterPhone;
        } else {
            this.roleLabel = "Người gửi yêu cầu";
            this.counterpartyName = this.ownerName;
            this.counterpartyStudentCode = this.ownerStudentCode;
            this.counterpartyPhone = this.ownerPhone;
        }

        this.ownerConfirmed = request.isOwnerConfirmed();
        this.requesterConfirmed = request.isRequesterConfirmed();

        boolean isPending = ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(this.status);
        boolean isAccepted = ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(this.status);
        boolean isItemAvailable = item != null && "AVAILABLE".equalsIgnoreCase(item.getStatus());

        this.canCancel = this.currentUserIsRequester && isPending;
        this.canAccept = this.currentUserIsOwner && isPending && isItemAvailable;
        this.canReject = this.currentUserIsOwner && isPending;

        if (isAccepted) {
            if (this.currentUserIsOwner) {
                this.canConfirmComplete = !this.ownerConfirmed;
            } else if (this.currentUserIsRequester) {
                this.canConfirmComplete = !this.requesterConfirmed;
            } else {
                this.canConfirmComplete = false;
            }
        } else {
            this.canConfirmComplete = false;
        }

        this.createdAt = request.getCreatedAt();
        this.processedAt = request.getProcessedAt() != null ? request.getProcessedAt() :
                (!isPending ? request.getUpdatedAt() : null);
        this.completedAt = request.getCompletedAt();
        this.updatedAt = request.getUpdatedAt();
    }

    public static String resolveTransactionTypeLabel(String type) {
        if (type == null) return "Trao đổi";
        return switch (type.toUpperCase()) {
            case "SELL" -> "Mua đồ";
            case "GIVEAWAY" -> "Nhận đồ miễn phí";
            case "EXCHANGE" -> "Trao đổi đồ";
            default -> type;
        };
    }

    public static String resolveStatusLabel(String status) {
        if (status == null) return "Không xác định";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xử lý";
            case "ACCEPTED" -> "Đã chấp nhận";
            case "REJECTED" -> "Đã từ chối";
            case "CANCELLED" -> "Đã hủy";
            case "COMPLETED" -> "Đã hoàn thành";
            default -> status;
        };
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public boolean isItemExists() { return itemExists; }
    public boolean isItemViewable() { return itemViewable; }
    public String getItemTitle() { return itemTitle; }
    public String getItemImagePath() { return itemImagePath; }
    public String getItemStatus() { return itemStatus; }
    public BigDecimal getItemPrice() { return itemPrice; }
    public String getItemDesiredItem() { return itemDesiredItem; }
    public String getTransactionType() { return transactionType; }
    public String getTransactionTypeLabel() { return transactionTypeLabel; }
    public String getOfferedItem() { return offeredItem; }
    public String getMessage() { return message; }
    public String getRejectReason() { return rejectReason; }
    public String getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerStudentCode() { return ownerStudentCode; }
    public String getOwnerPhone() { return ownerPhone; }
    public Long getRequesterId() { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public String getRequesterStudentCode() { return requesterStudentCode; }
    public String getRequesterPhone() { return requesterPhone; }
    public boolean isCurrentUserIsOwner() { return currentUserIsOwner; }
    public boolean isCurrentUserIsRequester() { return currentUserIsRequester; }
    public String getRoleLabel() { return roleLabel; }
    public String getCounterpartyName() { return counterpartyName; }
    public String getCounterpartyStudentCode() { return counterpartyStudentCode; }
    public String getCounterpartyPhone() { return counterpartyPhone; }
    public boolean isOwnerConfirmed() { return ownerConfirmed; }
    public boolean isRequesterConfirmed() { return requesterConfirmed; }
    public boolean isCanCancel() { return canCancel; }
    public boolean isCanAccept() { return canAccept; }
    public boolean isCanReject() { return canReject; }
    public boolean isCanConfirmComplete() { return canConfirmComplete; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

