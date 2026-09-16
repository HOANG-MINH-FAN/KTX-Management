package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * ============================================================
 * ENTITY: Student
 * ============================================================
 * Kế thừa từ lớp trừu tượng User (Inheritance + Encapsulation).
 *
 * Ngoài thông tin tài khoản từ User, Student còn có:
 * - Mã sinh viên (student_code) – định danh học vụ
 * - Thông tin cá nhân: họ tên, điện thoại, email, khoa, ngày sinh...
 *
 * Discriminator: role = 'STUDENT'
 * ============================================================
 */
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    /**
     * Mã số sinh viên do nhà trường cấp (VD: SV2024001).
     * Unique trong toàn hệ thống.
     */
    @Column(name = "student_code", unique = true, length = 50)
    private String studentCode;

    /**
     * Họ và tên đầy đủ của sinh viên.
     */
    @Column(name = "full_name", length = 255)
    private String fullName;

    /**
     * Số điện thoại liên hệ.
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Địa chỉ email.
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Khoa / ngành học (VD: Công nghệ thông tin, Kinh tế).
     */
    @Column(name = "faculty", length = 255)
    private String faculty;

    /**
     * Ngày sinh.
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Giới tính: MALE | FEMALE | OTHER
     */
    @Column(name = "gender", length = 10)
    private String gender;

    /**
     * Quê quán / địa chỉ thường trú.
     */
    @Column(name = "hometown", length = 500)
    private String hometown;

    // ===================== Constructors =====================

    public Student() {
        super();
    }

    public Student(String username, String password, String studentCode, String fullName) {
        super(username, password);
        this.studentCode = studentCode;
        this.fullName    = fullName;
    }

    // ===================== Getters & Setters =====================

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }

    @Override
    public String toString() {
        return "Student{id=" + getId() + ", studentCode='" + studentCode
                + "', fullName='" + fullName + "'}";
    }
}
