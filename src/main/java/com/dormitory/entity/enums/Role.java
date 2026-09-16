package com.dormitory.entity.enums;

/**
 * Vai trò người dùng trong hệ thống.
 * Dùng làm discriminator cho SINGLE_TABLE inheritance (User -> Admin / Student).
 */
public enum Role {
    /** Quản trị viên: có toàn quyền quản lý hệ thống */
    ADMIN,
    /** Sinh viên: chỉ xem thông tin cá nhân, gửi đăng ký, xem hóa đơn */
    STUDENT
}
