package com.dormitory.controller;

/**
 * AllocationController.java — Đã được thay thế bởi kiến trúc mới.
 *
 * Chức năng cũ (check-in thủ công qua Allocation entity) đã được thay thế bởi:
 * - RegistrationController: Sinh viên gửi đơn → Admin duyệt
 * - ContractController: Hợp đồng được tạo tự động khi duyệt đơn
 * - Room.incrementOccupied() / decrementOccupied(): Quản lý occupied tự động
 *
 * File này để trống để tránh lỗi compile.
 * Entity Allocation.java và AllocationRepository vẫn tồn tại nhưng không
 * được dùng trong hệ thống mới (backward compatibility).
 */
public class AllocationController {
    // Intentionally empty — replaced by RegistrationController + ContractController
}
