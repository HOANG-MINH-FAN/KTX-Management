package com.dormitory.entity.enums;

/**
 * Trạng thái đơn đăng ký phòng ở.
 *
 * Workflow: PENDING → APPROVED (tự động tạo Contract)
 *                   → REJECTED  (ghi admin_note)
 */
public enum RegistrationStatus {
    /** Chờ quản trị viên xét duyệt */
    PENDING,
    /** Đã được duyệt → hợp đồng được tạo tự động */
    APPROVED,
    /** Bị từ chối */
    REJECTED
}
