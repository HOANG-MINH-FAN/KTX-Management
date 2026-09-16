package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * LỚP PHÍ: WaterFee (Phí tiền nước)
 * ============================================================
 * Tính tiền nước theo chênh lệch chỉ số đồng hồ nước.
 *
 * Công thức: tiền nước = giá/m³ × (chỉ số mới - chỉ số cũ)
 *
 * Ví dụ: 10,000đ/m³ × (25 - 10) m³ = 150,000đ
 *
 * Override calculateFee() — đa hình từ AbstractFee.
 * ============================================================
 */
public class WaterFee extends AbstractFee {

    /** Chỉ số đồng hồ kỳ trước (m³). */
    private final int previousReading;

    /** Chỉ số đồng hồ kỳ này (m³). */
    private final int currentReading;

    /**
     * @param pricePerM3      Giá nước mỗi m³ (VND)
     * @param previousReading Chỉ số nước kỳ trước (m³)
     * @param currentReading  Chỉ số nước kỳ này (m³)
     */
    public WaterFee(BigDecimal pricePerM3, int previousReading, int currentReading) {
        super(
            buildDescription(pricePerM3, previousReading, currentReading),
            pricePerM3
        );
        if (currentReading < previousReading) {
            throw new IllegalArgumentException(
                "Chỉ số nước hiện tại (" + currentReading + ") không thể nhỏ hơn chỉ số cũ (" + previousReading + ")"
            );
        }
        this.previousReading = previousReading;
        this.currentReading  = currentReading;
    }

    private static String buildDescription(BigDecimal price, int prev, int curr) {
        int used = curr - prev;
        return String.format("Tiền nước: %.1f m³ (CS %d → %d) × %,.0f đ/m³", (double) used, prev, curr, price);
    }

    /**
     * Tính phí nước: giá/m³ × số m³ đã dùng.
     * Override từ AbstractFee — đa hình.
     */
    @Override
    public BigDecimal calculateFee() {
        int usedM3 = currentReading - previousReading;
        return unitPrice.multiply(BigDecimal.valueOf(usedM3));
    }

    @Override
    public String getFeeTypeName() {
        return "WATER";
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.valueOf(currentReading - previousReading);
    }

    public int getPreviousReading() { return previousReading; }
    public int getCurrentReading()  { return currentReading; }
    public int getUsedM3()          { return currentReading - previousReading; }
}
