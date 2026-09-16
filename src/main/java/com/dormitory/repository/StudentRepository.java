package com.dormitory.repository;

import com.dormitory.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Student entity.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Tìm sinh viên theo mã số sinh viên.
     */
    Optional<Student> findByStudentCode(String studentCode);

    /**
     * Tìm sinh viên theo username (tài khoản).
     */
    Optional<Student> findByUsername(String username);

    /**
     * Kiểm tra mã sinh viên đã tồn tại chưa.
     */
    boolean existsByStudentCode(String studentCode);

    /**
     * Tìm sinh viên theo tên (không phân biệt hoa thường, chứa chuỗi).
     */
    List<Student> findByFullNameContainingIgnoreCase(String name);

    /**
     * Tìm sinh viên theo khoa.
     */
    List<Student> findByFaculty(String faculty);

    /**
     * Kiểm tra username đã tồn tại chưa (inherited từ User).
     */
    boolean existsByUsername(String username);

    /**
     * Tổng số sinh viên đang ở KTX (có hợp đồng ACTIVE).
     */
    @Query("SELECT COUNT(DISTINCT c.student) FROM Contract c WHERE c.status = 'ACTIVE'")
    long countStudentsWithActiveContract();
}
