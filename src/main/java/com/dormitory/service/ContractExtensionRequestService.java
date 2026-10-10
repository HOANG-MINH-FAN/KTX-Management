package com.dormitory.service;

import com.dormitory.entity.ContractExtensionRequest;
import com.dormitory.entity.enums.RequestStatus;

import java.time.LocalDate;
import java.util.List;

public interface ContractExtensionRequestService {

    ContractExtensionRequest createRequest(Long studentId, Long contractId, LocalDate newEndDate, String reason);

    void approveRequest(Long requestId, String adminUsername);

    void rejectRequest(Long requestId, String rejectReason, String adminUsername);

    List<ContractExtensionRequest> findByStudentId(Long studentId);

    List<ContractExtensionRequest> findAll();

    List<ContractExtensionRequest> findByStatus(RequestStatus status);
}
