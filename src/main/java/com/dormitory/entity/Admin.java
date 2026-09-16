package com.dormitory.entity;

import jakarta.persistence.*;

/**
 * ============================================================
 * ENTITY: Admin
 * ============================================================
 * Kế thừa từ lớp trừu tượng User (Inheritance).
 *
 * Admin là quản trị viên hệ thống:
 * - Có toàn quyền CRUD: sinh viên, phòng, tòa nhà
 * - Duyệt/từ chối đơn đăng ký
 * - Tạo và quản lý hóa đơn
 * - Ghi nhận vi phạm
 *
 * Lưu ý: Admin không có thêm trường dữ liệu riêng,
 * chỉ được phân biệt bằng discriminator role='ADMIN'.
 * ============================================================
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    // Admin không có thêm trường riêng ngoài User.
    // Có thể mở rộng sau: thêm trường department, phone...

    public Admin() {
        super();
    }

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String toString() {
        return "Admin{id=" + getId() + ", username='" + getUsername() + "'}";
    }
}
