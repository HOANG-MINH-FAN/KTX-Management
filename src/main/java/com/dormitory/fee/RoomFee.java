package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * LỚP PHÍ: RoomFee (Phí tiền phòng)
 * ============================================================
 * Tính tiền phòng cố định theo số tháng.
 *
 * Công thức: tiền phòng = giá/tháng × số tháng
 *
 * Ví dụ: phòng 800,000đ/tháng × 1 tháng = 800,000đ
 * ============================================================
 */
public class RoomFee extends AbstractFee {

    /**
     * Số tháng trong kỳ hóa đơn (thường = 1).
     */
    private final int months;

    /**
     * @param monthlyRate Giá phòng mỗi tháng (VND)
     * @param months      Số tháng tính phí (thường = 1)
     */
    public RoomFee(BigDecimal monthlyRate, int months) {
        super(
            buildDescription(monthlyRate, months),
            monthlyRate
        );
        if (months <= 0) {
            throw new IllegalArgumentException("Số tháng phải > 0");
        }
        this.months = months;
    }

    private static String buildDescription(BigDecimal monthlyRate, int months) {
        return String.format("Tiền phòng %d tháng × %,.0f đ/tháng", months, monthlyRate);
    }

    /**
     * Tính phí phòng: giá/tháng × số tháng.
     *
     * Đa hình (override): lớp cha gọi calculateFee()
     * mà không cần biết đây là phí phòng hay phí điện.
     */
    @Override
    public BigDecimal calculateFee() {
        return unitPrice.multiply(BigDecimal.valueOf(months));
    }

    @Override
    public String getFeeTypeName() {
        return "ROOM";
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.valueOf(months);
    }

    public int getMonths() { return months; }
}
