package com.dormitory.entity.enums;

/**
 * Phương thức thanh toán hóa đơn.
 */
public enum PaymentMethod {
    /** Thanh toán tiền mặt tại quầy */
    CASH("Tiền mặt"),
    /** Chuyển khoản ngân hàng */
    BANK_TRANSFER("Chuyển khoản"),
    /** Ví điện tử MoMo */
    MOMO("MoMo"),
    /** Cổng thanh toán VNPay */
    VNPAY("VNPay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
