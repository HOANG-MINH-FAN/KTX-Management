package com.dormitory.exchange.dto;

import com.dormitory.exchange.entity.ExchangeItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu từ form đăng tin và sửa tin của sinh viên.
 * Ngăn chặn việc truyền trực tiếp ownerId hoặc status từ client.
 */
public class ExchangeItemForm {

    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải từ 3 đến 255 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 50, message = "Danh mục không được vượt quá 50 ký tự")
    private String category;

    @NotBlank(message = "Vui lòng chọn hình thức")
    private String exchangeType; // SELL, EXCHANGE, GIVEAWAY

    @NotBlank(message = "Vui lòng chọn tình trạng món đồ")
    private String itemCondition; // NEW, LIKE_NEW, USED

    private BigDecimal price;

    @Size(max = 500, message = "Món muốn đổi không được vượt quá 500 ký tự")
    private String desiredItem;

    private String existingImagePath;

    public ExchangeItemForm() {
    }

    public static ExchangeItemForm fromEntity(ExchangeItem item) {
        ExchangeItemForm form = new ExchangeItemForm();
        form.setId(item.getId());
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setCategory(item.getCategory());
        form.setExchangeType(item.getExchangeType());
        form.setItemCondition(item.getItemCondition());
        form.setPrice(item.getPrice());
        form.setDesiredItem(item.getDesiredItem());
        form.setExistingImagePath(item.getImagePath());
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExistingImagePath() {
        return existingImagePath;
    }

    public void setExistingImagePath(String existingImagePath) {
        this.existingImagePath = existingImagePath;
    }
}
