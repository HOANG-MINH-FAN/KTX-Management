package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * LỚP PHÍ: ServiceFee (Phí dịch vụ phát sinh)
 * ============================================================
 * Phí dịch vụ cố định không theo chỉ số:
 * - Phí internet
 * - Phí vệ sinh môi trường
 * - Phí bảo vệ an ninh
 * - Các phí dịch vụ khác
 *
 * Công thức: tiền = amount (cố định)
 *
 * Override calculateFee() — đa hình từ AbstractFee.
 * ============================================================
 */
public class ServiceFee extends AbstractFee {

    /**
     * @param serviceName Tên dịch vụ (VD: "Phí internet", "Phí vệ sinh")
     * @param amount      Số tiền cố định (VND)
     */
    public ServiceFee(String serviceName, BigDecimal amount) {
        super(serviceName, amount);
    }

    /**
     * Phí dịch vụ = amount cố định (unitPrice).
     * Override từ AbstractFee — đa hình.
     */
    @Override
    public BigDecimal calculateFee() {
        // Phí dịch vụ là số tiền cố định, không nhân với số lượng
        return unitPrice;
    }

    @Override
    public String getFeeTypeName() {
        return "SERVICE";
    }

    // Quantity = 1 (cố định), dùng mặc định từ AbstractFee
}
