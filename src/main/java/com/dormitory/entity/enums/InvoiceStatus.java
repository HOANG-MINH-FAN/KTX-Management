package com.dormitory.entity.enums;

/**
 * Trạng thái hóa đơn.
 */
public enum InvoiceStatus {
    /** Chưa thanh toán */
    UNPAID,
    /** Đã thanh toán đủ */
    PAID,
    /** Quá hạn thanh toán (due_date đã qua mà vẫn UNPAID) */
    OVERDUE
}
