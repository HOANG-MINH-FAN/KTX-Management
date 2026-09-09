package com.dormitory.repository;

import com.dormitory.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Integer> {

    // все текущие (можно показывать все allocations)
    List<Allocation> findAllByOrderByIdDesc();

    // чтобы запретить повторный check-in того же студента (если нужно)
    boolean existsByStudentId(Integer studentId);

    long countByStatus(String status);
}



