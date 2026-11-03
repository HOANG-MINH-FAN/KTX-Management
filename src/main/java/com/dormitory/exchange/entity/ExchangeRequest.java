package com.dormitory.exchange.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "transaction_type", length = 20)
    private String transactionType;

    @Column(name = "offered_item", length = 500)
    private String offeredItem;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(nullable = false)
    private String status = STATUS_PENDING;

    @Column(name = "owner_confirmed")
    private Boolean ownerConfirmed = false;

    @Column(name = "requester_confirmed")
    private Boolean requesterConfirmed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (ownerConfirmed == null) {
            ownerConfirmed = false;
        }
        if (requesterConfirmed == null) {
            requesterConfirmed = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getOfferedItem() {
        return offeredItem;
    }

    public void setOfferedItem(String offeredItem) {
        this.offeredItem = offeredItem;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOwnerConfirmed() {
        return ownerConfirmed != null && ownerConfirmed;
    }

    public boolean isOwnerConfirmed() {
        return Boolean.TRUE.equals(ownerConfirmed);
    }

    public void setOwnerConfirmed(Boolean ownerConfirmed) {
        this.ownerConfirmed = ownerConfirmed != null && ownerConfirmed;
    }

    public Boolean getRequesterConfirmed() {
        return requesterConfirmed != null && requesterConfirmed;
    }

    public boolean isRequesterConfirmed() {
        return Boolean.TRUE.equals(requesterConfirmed);
    }

    public void setRequesterConfirmed(Boolean requesterConfirmed) {
        this.requesterConfirmed = requesterConfirmed != null && requesterConfirmed;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
