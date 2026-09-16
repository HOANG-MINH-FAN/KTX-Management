-- =============================================================
-- HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ SINH VIÊN
-- dormitory_db.sql  –  DDL + Seed Data hoàn chỉnh
-- Phiên bản: 2.0 (nâng cấp từ schema cơ bản)
--
-- Cách chạy:
--   mysql -u root -p < dormitory_db.sql
--
-- MẬT KHẨU MẪU (BCrypt):
--   admin    / admin@123
--   sv001    / sv@123456
--   sv002    / sv@123456
--   sv003    / sv@123456
-- (BCrypt hash được sinh bởi DataInitializer.java khi khởi động lần đầu)
-- =============================================================

CREATE DATABASE IF NOT EXISTS dormitory_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE dormitory_db;

-- Tắt kiểm tra FK tạm thời để drop/create dễ dàng
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================
-- XÓA BẢNG CŨ (thứ tự từ phụ thuộc -> độc lập)
-- =============================================================
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS invoice_details;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS violations;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS buildings;
DROP TABLE IF EXISTS users;
-- Xóa bảng cũ của schema v1
DROP TABLE IF EXISTS allocations;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS students;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- BẢNG 1: users
-- Chứa tất cả tài khoản (ADMIN và STUDENT).
-- Sử dụng SINGLE_TABLE inheritance trong JPA:
--   cột `role` là discriminator (ADMIN | STUDENT).
-- Cột student_* chỉ dùng cho role=STUDENT (null với ADMIN).
-- =============================================================
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL COMMENT 'BCrypt encoded password',
    role            VARCHAR(20)     NOT NULL DEFAULT 'ADMIN' COMMENT 'ADMIN | STUDENT',
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Thông tin sinh viên (null nếu role = ADMIN)
    student_code    VARCHAR(50)     NULL COMMENT 'Mã số sinh viên, VD: SV2024001',
    full_name       VARCHAR(255)    NULL COMMENT 'Họ và tên đầy đủ',
    phone           VARCHAR(20)     NULL,
    email           VARCHAR(255)    NULL,
    faculty         VARCHAR(255)    NULL COMMENT 'Khoa / ngành học',
    date_of_birth   DATE            NULL,
    gender          VARCHAR(10)     NULL COMMENT 'MALE | FEMALE | OTHER',
    hometown        VARCHAR(500)    NULL COMMENT 'Quê quán',

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username    (username),
    UNIQUE KEY uk_users_student_code (student_code),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng tài khoản người dùng (ADMIN + STUDENT, SINGLE_TABLE)';

-- =============================================================
-- BẢNG 2: buildings
-- Thông tin các tòa nhà ký túc xá.
-- =============================================================
CREATE TABLE buildings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL COMMENT 'Tên tòa nhà, VD: Tòa A, Tòa B',
    address         VARCHAR(500)    NULL,
    total_floors    INT             NOT NULL DEFAULT 1,
    description     TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_buildings_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng tòa nhà ký túc xá';

-- =============================================================
-- BẢNG 3: rooms
-- Thông tin phòng trong từng tòa nhà.
-- =============================================================
CREATE TABLE rooms (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    building_id         BIGINT          NOT NULL COMMENT 'FK -> buildings.id',
    room_number         VARCHAR(20)     NOT NULL COMMENT 'Số phòng, VD: 101, 202',
    floor               INT             NOT NULL DEFAULT 1,
    capacity            INT             NOT NULL DEFAULT 4 COMMENT 'Số chỗ tối đa',
    occupied            INT             NOT NULL DEFAULT 0 COMMENT 'Số chỗ đã chiếm',
    room_type           VARCHAR(20)     NOT NULL DEFAULT 'MIXED' COMMENT 'MALE | FEMALE | MIXED',
    room_fee_per_month  DECIMAL(15,2)   NOT NULL DEFAULT 0.00 COMMENT 'Tiền phòng tháng (VND)',
    status              VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE | FULL | MAINTENANCE',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_rooms_building_number (building_id, room_number),
    INDEX idx_rooms_status (status),
    CONSTRAINT fk_rooms_building
        FOREIGN KEY (building_id) REFERENCES buildings(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng phòng ở';

-- =============================================================
-- BẢNG 4: registrations
-- Đơn đăng ký phòng của sinh viên.
-- Workflow: PENDING -> APPROVED (tạo Contract) | REJECTED
-- =============================================================
CREATE TABLE registrations (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    student_id          BIGINT          NOT NULL COMMENT 'FK -> users.id (role=STUDENT)',
    room_id             BIGINT          NOT NULL COMMENT 'FK -> rooms.id',
    desired_start_date  DATE            NOT NULL COMMENT 'Ngày muốn vào ở',
    note                TEXT            NULL COMMENT 'Ghi chú của sinh viên',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | APPROVED | REJECTED',
    admin_note          TEXT            NULL COMMENT 'Ghi chú của quản trị viên khi duyệt/từ chối',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_reg_student (student_id),
    INDEX idx_reg_status  (status),
    CONSTRAINT fk_reg_student
        FOREIGN KEY (student_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reg_room
        FOREIGN KEY (room_id) REFERENCES rooms(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đơn đăng ký phòng ở';

-- =============================================================
-- BẢNG 5: contracts
-- Hợp đồng ở của sinh viên.
-- Tự động tạo khi admin APPROVED một registration.
-- =============================================================
CREATE TABLE contracts (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL COMMENT 'FK -> users.id (role=STUDENT)',
    room_id         BIGINT          NOT NULL COMMENT 'FK -> rooms.id',
    registration_id BIGINT          NULL COMMENT 'FK -> registrations.id (nguồn gốc hợp đồng)',
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    monthly_fee     DECIMAL(15,2)   NOT NULL COMMENT 'Tiền phòng/tháng tại thời điểm ký',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | EXPIRED | TERMINATED',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_contract_student (student_id),
    INDEX idx_contract_status  (status),
    CONSTRAINT fk_contract_student
        FOREIGN KEY (student_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_contract_room
        FOREIGN KEY (room_id) REFERENCES rooms(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_contract_registration
        FOREIGN KEY (registration_id) REFERENCES registrations(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Hợp đồng thuê phòng';

-- =============================================================
-- BẢNG 6: invoices
-- Hóa đơn theo tháng cho từng hợp đồng.
-- =============================================================
CREATE TABLE invoices (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    contract_id     BIGINT          NOT NULL COMMENT 'FK -> contracts.id',
    period_month    TINYINT         NOT NULL COMMENT 'Tháng (1-12)',
    period_year     SMALLINT        NOT NULL COMMENT 'Năm (VD: 2024)',
    total_amount    DECIMAL(15,2)   NOT NULL DEFAULT 0.00 COMMENT 'Tổng tiền phải trả (VND)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID | PAID | OVERDUE',
    due_date        DATE            NOT NULL COMMENT 'Hạn thanh toán',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_contract_period (contract_id, period_month, period_year),
    INDEX idx_invoice_status (status),
    CONSTRAINT fk_invoice_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Hóa đơn tháng';

-- =============================================================
-- BẢNG 7: invoice_details
-- Chi tiết từng khoản phí trong hóa đơn.
-- Ánh xạ với các lớp Fee trong OOP (RoomFee, ElectricityFee...)
-- =============================================================
CREATE TABLE invoice_details (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    invoice_id  BIGINT          NOT NULL COMMENT 'FK -> invoices.id',
    fee_type    VARCHAR(20)     NOT NULL COMMENT 'ROOM | ELECTRICITY | WATER | SERVICE | VIOLATION',
    description VARCHAR(500)    NULL COMMENT 'Mô tả: VD "Điện tháng 9: 150 kWh x 3,500đ"',
    quantity    DECIMAL(10,2)   NOT NULL DEFAULT 1.00 COMMENT 'Số lượng (kWh, m3, tháng...)',
    unit_price  DECIMAL(15,2)   NOT NULL COMMENT 'Đơn giá',
    amount      DECIMAL(15,2)   NOT NULL COMMENT 'Thành tiền = quantity * unit_price',

    PRIMARY KEY (id),
    INDEX idx_detail_invoice (invoice_id),
    CONSTRAINT fk_detail_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Chi tiết khoản phí trong hóa đơn';

-- =============================================================
-- BẢNG 8: payments
-- Lịch sử thanh toán hóa đơn.
-- =============================================================
CREATE TABLE payments (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    invoice_id      BIGINT          NOT NULL COMMENT 'FK -> invoices.id',
    amount_paid     DECIMAL(15,2)   NOT NULL COMMENT 'Số tiền đã thanh toán',
    payment_date    DATE            NOT NULL,
    payment_method  VARCHAR(20)     NOT NULL DEFAULT 'CASH' COMMENT 'CASH | BANK_TRANSFER | MOMO | VNPAY',
    note            VARCHAR(500)    NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_payment_invoice (invoice_id),
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Lịch sử thanh toán hóa đơn';

-- =============================================================
-- BẢNG 9: violations
-- Ghi nhận vi phạm nội quy của sinh viên.
-- =============================================================
CREATE TABLE violations (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL COMMENT 'FK -> users.id (role=STUDENT)',
    description     TEXT            NOT NULL COMMENT 'Mô tả hành vi vi phạm',
    violation_date  DATE            NOT NULL COMMENT 'Ngày vi phạm',
    fine_amount     DECIMAL(15,2)   NOT NULL DEFAULT 0.00 COMMENT 'Mức phạt (VND)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID | PAID | WAIVED',
    note            TEXT            NULL COMMENT 'Ghi chú bổ sung',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_violation_student (student_id),
    INDEX idx_violation_status  (status),
    CONSTRAINT fk_violation_student
        FOREIGN KEY (student_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng vi phạm nội quy';

-- =============================================================
-- DỮ LIỆU MẪU (SEED DATA)
-- Lưu ý: Mật khẩu sẽ được DataInitializer.java mã hóa BCrypt
--        khi khởi động lần đầu nếu bảng users còn trống.
--        Dữ liệu dưới đây dùng placeholder hash.
--        Tham khảo DataInitializer.java để xem mật khẩu gốc.
-- =============================================================

-- Tòa nhà mẫu
INSERT INTO buildings (name, address, total_floors, description) VALUES
('Tòa A', 'Khu A, Ký túc xá Đại học XYZ', 5, 'Dành cho sinh viên nam, 5 tầng, 20 phòng'),
('Tòa B', 'Khu B, Ký túc xá Đại học XYZ', 4, 'Dành cho sinh viên nữ, 4 tầng, 16 phòng'),
('Tòa C', 'Khu C, Ký túc xá Đại học XYZ', 3, 'Phòng hỗn hợp, 3 tầng, tiện nghi cao cấp');

-- Phòng mẫu
INSERT INTO rooms (building_id, room_number, floor, capacity, occupied, room_type, room_fee_per_month, status) VALUES
-- Tòa A (Nam)
(1, '101', 1, 4, 0, 'MALE', 800000,  'AVAILABLE'),
(1, '102', 1, 4, 0, 'MALE', 800000,  'AVAILABLE'),
(1, '201', 2, 4, 0, 'MALE', 800000,  'AVAILABLE'),
(1, '202', 2, 4, 0, 'MALE', 800000,  'AVAILABLE'),
(1, '301', 3, 2, 0, 'MALE', 1200000, 'AVAILABLE'),
-- Tòa B (Nữ)
(2, '101', 1, 4, 0, 'FEMALE', 800000,  'AVAILABLE'),
(2, '102', 1, 4, 0, 'FEMALE', 800000,  'AVAILABLE'),
(2, '201', 2, 4, 0, 'FEMALE', 850000,  'AVAILABLE'),
-- Tòa C (Mixed, cao cấp)
(3, '101', 1, 2, 0, 'MIXED', 1500000, 'AVAILABLE'),
(3, '201', 2, 2, 0, 'MIXED', 1500000, 'AVAILABLE'),
(3, '202', 2, 2, 0, 'MIXED', 1500000, 'MAINTENANCE');

-- =============================================================
-- Ghi chú: Tài khoản người dùng (users) và dữ liệu nghiệp vụ
-- mẫu (hợp đồng, hóa đơn, vi phạm) sẽ được tạo tự động bởi
-- DataInitializer.java khi khởi động ứng dụng lần đầu.
-- =============================================================
