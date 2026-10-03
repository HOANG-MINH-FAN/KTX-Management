package com.dormitory.exchange.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_items")
public class ExchangeItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "exchange_type", nullable = false)
    private String exchangeType;

    @Column(name = "item_condition", nullable = false)
    private String itemCondition;

    private BigDecimal price;

    @Column(name = "desired_item")
    private String desiredItem;

    @Column(name = "image_path")
    private String imagePath;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

   public Long getId() {
    return id;
}

public Long getOwnerId() {
    return ownerId;
}

public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
}

public String getTitle() {
    return title;
}

public void setTitle(String title) {
    this.title = title;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public String getCategory() {
    return category;
}

public void setCategory(String category) {
    this.category = category;
}

public String getExchangeType() {
    return exchangeType;
}

public void setExchangeType(String exchangeType) {
    this.exchangeType = exchangeType;
}

public String getItemCondition() {
    return itemCondition;
}

public void setItemCondition(String itemCondition) {
    this.itemCondition = itemCondition;
}

public BigDecimal getPrice() {
    return price;
}

public void setPrice(BigDecimal price) {
    this.price = price;
}

public String getDesiredItem() {
    return desiredItem;
}

public void setDesiredItem(String desiredItem) {
    this.desiredItem = desiredItem;
}

public String getImagePath() {
    return imagePath;
}

public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

public LocalDateTime getUpdatedAt() {
    return updatedAt;
}
}
