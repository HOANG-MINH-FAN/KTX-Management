package com.dormitory.entity.enums;

/**
 * Loại phí trong chi tiết hóa đơn.
 * Mỗi loại tương ứng với một lớp con cụ thể trong hệ thống phí OOP:
 *   ROOM        -> RoomFee
 *   ELECTRICITY -> ElectricityFee
 *   WATER       -> WaterFee
 *   SERVICE     -> ServiceFee
 *   VIOLATION   -> ViolationFee
 */
public enum FeeType {
    /** Phí tiền phòng cố định theo tháng */
    ROOM("Tiền phòng"),
    /** Phí điện tính theo chỉ số công-tơ */
    ELECTRICITY("Tiền điện"),
    /** Phí nước tính theo chỉ số đồng hồ */
    WATER("Tiền nước"),
    /** Phí dịch vụ phát sinh (internet, vệ sinh, an ninh...) */
    SERVICE("Phí dịch vụ"),
    /** Phí phạt vi phạm nội quy */
    VIOLATION("Tiền phạt vi phạm");

    private final String displayName;

    FeeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
