package com.dormitory.fee;

import java.math.BigDecimal;

/**
 * ============================================================
 * LỚP PHÍ: ViolationFee (Phí phạt vi phạm nội quy)
 * ============================================================
 * Tính phí phạt từ một vi phạm cụ thể.
 * Liên kết logic với entity Violation.
 *
 * Mức phạt được lấy trực tiếp từ violation.fineAmount.
 *
 * Công thức: tiền phạt = fine_amount từ Violation
 *
 * Override calculateFee() — đa hình từ AbstractFee.
 * ============================================================
 */
public class ViolationFee extends AbstractFee {

    /** Mã vi phạm để tham chiếu. */
    private final Long violationId;

    /**
     * @param violationDescription Mô tả vi phạm
     * @param fineAmount           Mức phạt (VND)
     * @param violationId          ID của Violation entity
     */
    public ViolationFee(String violationDescription, BigDecimal fineAmount, Long violationId) {
        super("Phạt vi phạm: " + violationDescription, fineAmount);
        this.violationId = violationId;
    }

    /**
     * Tiền phạt = fineAmount cố định (unitPrice).
     * Override từ AbstractFee — đa hình.
     */
    @Override
    public BigDecimal calculateFee() {
        return unitPrice;  // Mức phạt cố định từ Violation.fineAmount
    }

    @Override
    public String getFeeTypeName() {
        return "VIOLATION";
    }

    public Long getViolationId() { return violationId; }
}
