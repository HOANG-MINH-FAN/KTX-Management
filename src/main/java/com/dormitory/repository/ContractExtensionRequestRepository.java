package com.dormitory.repository;

import com.dormitory.entity.ContractExtensionRequest;
import com.dormitory.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractExtensionRequestRepository extends JpaRepository<ContractExtensionRequest, Long> {

    List<ContractExtensionRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT COUNT(r) > 0 FROM ContractExtensionRequest r WHERE r.contract.id = :contractId AND r.status = 'PENDING'")
    boolean existsPendingRequestByContractId(@Param("contractId") Long contractId);

    List<ContractExtensionRequest> findAllByOrderByCreatedAtDesc();

    List<ContractExtensionRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}
