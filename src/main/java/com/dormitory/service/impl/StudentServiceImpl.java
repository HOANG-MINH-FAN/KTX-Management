package com.dormitory.service.impl;

import com.dormitory.entity.Student;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode);
    }

    @Override
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.existsByStudentCode(studentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return studentRepository.existsByUsername(username);
    }
}
