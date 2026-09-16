package com.dormitory.entity.enums;

/**
 * Trạng thái phòng ở.
 */
public enum RoomStatus {
    /** Còn chỗ trống, có thể nhận thêm sinh viên */
    AVAILABLE,
    /** Phòng đã đầy (occupied == capacity) */
    FULL,
    /** Đang bảo trì, tạm thời không nhận sinh viên */
    MAINTENANCE
}
