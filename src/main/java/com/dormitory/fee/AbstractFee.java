package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * ABSTRACT CLASS: AbstractFee (Phí trừu tượng)
 * ============================================================
 * Áp dụng đầy đủ 4 nguyên lý OOP:
 *
 * 1. TRỪU TƯỢNG HÓA (Abstraction):
 *    - Lớp abstract, không khởi tạo trực tiếp.
 *    - Định nghĩa "hợp đồng" chung: mọi loại phí đều có
 *      description, unitPrice, và phải tính được calculateFee().
 *
 * 2. ĐÓNG GÓI (Encapsulation):
 *    - Trường protected — chỉ lớp con và cùng package truy cập.
 *    - Không có setter cho unitPrice và description sau khi khởi tạo
 *      (immutable design, tránh thay đổi ngoài ý muốn).
 *
 * 3. KẾ THỪA (Inheritance):
 *    - RoomFee, ElectricityFee, WaterFee, ServiceFee, ViolationFee
 *      kế thừa và dùng lại description + unitPrice từ AbstractFee.
 *
 * 4. ĐA HÌNH (Polymorphism):
 *    - Phương thức calculateFee() là abstract, mỗi lớp con
 *      override theo công thức tính phí riêng của mình.
 *    - InvoiceService có thể dùng List<AbstractFee> để tính
 *      tất cả khoản phí mà không cần biết loại cụ thể.
 *
 * Ví dụ đa hình trong InvoiceService:
 * <pre>
 *   List<AbstractFee> fees = List.of(
 *       new RoomFee(...),
 *       new ElectricityFee(...),
 *       new WaterFee(...)
 *   );
 *   BigDecimal total = fees.stream()
 *       .map(AbstractFee::calculateFee)   // gọi qua đa hình
 *       .reduce(BigDecimal.ZERO, BigDecimal::add);
 * </pre>
 * ============================================================
 */
public abstract class AbstractFee {

    /** Mô tả khoản phí để hiển thị trên hóa đơn. */
    protected final String description;

    /** Đơn giá cơ bản (VND/đơn vị). */
    protected final BigDecimal unitPrice;

    // ===================== Constructor =====================

    /**
     * Constructor cơ sở — lớp con phải gọi super().
     *
     * @param description Mô tả khoản phí
     * @param unitPrice   Đơn giá (phải >= 0)
     */
    protected AbstractFee(String description, BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm: " + unitPrice);
        }
        this.description = description;
        this.unitPrice   = unitPrice;
    }

    // ===================== Abstract Methods =====================

    /**
     * Tính tổng tiền khoản phí này.
     *
     * Mỗi lớp con override phương thức này với công thức riêng:
     * - RoomFee:        unitPrice × số tháng
     * - ElectricityFee: unitPrice × (chỉ số mới - chỉ số cũ)
     * - WaterFee:       unitPrice × (chỉ số mới - chỉ số cũ)
     * - ServiceFee:     fixed amount
     * - ViolationFee:   fine amount từ Violation entity
     *
     * @return Số tiền phải trả (VND), >= 0
     */
    public abstract BigDecimal calculateFee();

    /**
     * Trả về loại phí dưới dạng chuỗi khớp với FeeType enum.
     * Dùng khi tạo InvoiceDetail.feeType.
     *
     * @return "ROOM" | "ELECTRICITY" | "WATER" | "SERVICE" | "VIOLATION"
     */
    public abstract String getFeeTypeName();

    // ===================== Common Methods =====================

    /**
     * Lấy mô tả khoản phí.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Lấy đơn giá.
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Lấy số lượng (đơn vị tính phí).
     * Mặc định là 1, lớp con override nếu cần.
     */
    public BigDecimal getQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{description='" + description
                + "', unitPrice=" + unitPrice
                + ", fee=" + calculateFee() + "}";
    }
}
