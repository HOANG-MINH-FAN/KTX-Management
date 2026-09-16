package com.dormitory.repository;

import com.dormitory.entity.Contract;
import com.dormitory.entity.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Contract (hợp đồng ở).
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * Lấy tất cả hợp đồng của một sinh viên.
     */
    List<Contract> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Lấy hợp đồng đang ACTIVE của một sinh viên (chỉ có 1).
     */
    Optional<Contract> findByStudentIdAndStatus(Long studentId, ContractStatus status);

    /**
     * Kiểm tra sinh viên có hợp đồng ACTIVE không.
     */
    boolean existsByStudentIdAndStatus(Long studentId, ContractStatus status);

    /**
     * Lấy tất cả hợp đồng ACTIVE.
     */
    List<Contract> findByStatusOrderByCreatedAtDesc(ContractStatus status);

    /**
     * Đếm hợp đồng theo trạng thái.
     */
    long countByStatus(ContractStatus status);

    /**
     * Lấy hợp đồng ACTIVE của một phòng.
     */
    List<Contract> findByRoomIdAndStatus(Long roomId, ContractStatus status);
}
