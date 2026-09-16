package com.dormitory.service;

import com.dormitory.entity.Student;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho sinh viên.
 */
public interface StudentService {

    List<Student> findAll();

    Optional<Student> findById(Long id);

    Optional<Student> findByUsername(String username);

    Optional<Student> findByStudentCode(String studentCode);

    Student save(Student student);

    void deleteById(Long id);

    boolean existsByStudentCode(String studentCode);

    boolean existsByUsername(String username);
}
