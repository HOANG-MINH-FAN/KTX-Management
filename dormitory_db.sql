-- =============================================================
-- Dormitory Information System - Database Schema
-- ITEC313 Project
--
-- Creates the `dormitory_db` database and all tables required
-- by the Spring Boot application (spring.jpa.hibernate.ddl-auto=none,
-- so this script must be run manually before starting the app).
--
-- Usage:
--   mysql -u root -p < dormitory_db.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS dormitory_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE dormitory_db;

-- -------------------------------------------------------------
-- Table: admins
-- Maps to com.dormitory.entity.Admin
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table: students
-- Maps to com.dormitory.entity.Student
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    faculty VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table: rooms
-- Maps to com.dormitory.entity.Room
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    building VARCHAR(255),
    room_number VARCHAR(50) NOT NULL,
    floor INT,
    capacity INT,
    occupied INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'AVAILABLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table: allocations
-- Maps to com.dormitory.entity.Allocation
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    room_id INT,
    check_in_date DATE,
    status VARCHAR(50),
    CONSTRAINT fk_allocations_student FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_allocations_room FOREIGN KEY (room_id) REFERENCES rooms(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Sample seed data (optional, safe to remove/edit)
-- -------------------------------------------------------------

-- Default admin account (username: admin / password: admin123)
INSERT INTO admins (username, password) VALUES ('admin', 'admin123');

-- Sample rooms
INSERT INTO rooms (building, room_number, floor, capacity, occupied, status) VALUES
('A', '101', 1, 2, 0, 'AVAILABLE'),
('A', '102', 1, 2, 0, 'AVAILABLE'),
('B', '201', 2, 4, 0, 'AVAILABLE');

-- Sample students
INSERT INTO students (student_id, full_name, phone, email, faculty) VALUES
('S001', 'John Doe', '0000000001', 'john.doe@example.com', 'Computer Science'),
('S002', 'Jane Smith', '0000000002', 'jane.smith@example.com', 'Business');
