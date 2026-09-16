package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * LỚP PHÍ: ElectricityFee (Phí tiền điện)
 * ============================================================
 * Tính tiền điện theo chênh lệch chỉ số công-tơ điện.
 *
 * Công thức: tiền điện = giá/kWh × (chỉ số mới - chỉ số cũ)
 *
 * Ví dụ: 3,500đ/kWh × (350 - 200) kWh = 525,000đ
 *
 * Override calculateFee() — đa hình từ AbstractFee.
 * ============================================================
 */
public class ElectricityFee extends AbstractFee {

    /** Chỉ số công-tơ kỳ trước (kWh). */
    private final int previousReading;

    /** Chỉ số công-tơ kỳ này (kWh). */
    private final int currentReading;

    /**
     * @param pricePerKwh     Giá điện mỗi kWh (VND)
     * @param previousReading Chỉ số điện kỳ trước (kWh)
     * @param currentReading  Chỉ số điện kỳ này (kWh)
     */
    public ElectricityFee(BigDecimal pricePerKwh, int previousReading, int currentReading) {
        super(
            buildDescription(pricePerKwh, previousReading, currentReading),
            pricePerKwh
        );
        if (currentReading < previousReading) {
            throw new IllegalArgumentException(
                "Chỉ số điện hiện tại (" + currentReading + ") không thể nhỏ hơn chỉ số cũ (" + previousReading + ")"
            );
        }
        this.previousReading = previousReading;
        this.currentReading  = currentReading;
    }

    private static String buildDescription(BigDecimal price, int prev, int curr) {
        int used = curr - prev;
        return String.format("Tiền điện: %d kWh (CS %d → %d) × %,.0f đ/kWh", used, prev, curr, price);
    }

    /**
     * Tính phí điện: giá/kWh × số kWh đã dùng.
     * Override từ AbstractFee — đa hình.
     */
    @Override
    public BigDecimal calculateFee() {
        int usedKwh = currentReading - previousReading;
        return unitPrice.multiply(BigDecimal.valueOf(usedKwh));
    }

    @Override
    public String getFeeTypeName() {
        return "ELECTRICITY";
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.valueOf(currentReading - previousReading);
    }

    public int getPreviousReading() { return previousReading; }
    public int getCurrentReading()  { return currentReading; }
    public int getUsedKwh()         { return currentReading - previousReading; }
}
