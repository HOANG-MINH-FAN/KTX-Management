package com.dormitory.service;

import com.dormitory.entity.Contract;
import com.dormitory.entity.enums.ContractStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho hợp đồng ở.
 */
public interface ContractService {

    List<Contract> findAll();

    Optional<Contract> findById(Long id);

    /** Lấy hợp đồng ACTIVE của sinh viên. */
    Optional<Contract> findActiveContractByStudentId(Long studentId);

    /** Lấy tất cả hợp đồng của sinh viên. */
    List<Contract> findByStudentId(Long studentId);

    /** Kết thúc sớm hợp đồng (TERMINATED). */
    void terminateContract(Long contractId);

    long countByStatus(ContractStatus status);
}
