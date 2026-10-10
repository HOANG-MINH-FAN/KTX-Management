package com.dormitory.service.impl;

import com.dormitory.entity.Contract;
import com.dormitory.entity.ContractExtensionRequest;
import com.dormitory.entity.Student;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.entity.enums.RequestStatus;
import com.dormitory.repository.ContractExtensionRequestRepository;
import com.dormitory.repository.ContractRepository;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.ContractExtensionRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ContractExtensionRequestServiceImpl implements ContractExtensionRequestService {

    private final ContractExtensionRequestRepository requestRepository;
    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;

    public ContractExtensionRequestServiceImpl(ContractExtensionRequestRepository requestRepository,
                                               ContractRepository contractRepository,
                                               StudentRepository studentRepository) {
        this.requestRepository = requestRepository;
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public ContractExtensionRequest createRequest(Long studentId, Long contractId, LocalDate newEndDate, String reason) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hợp đồng."));

        if (!contract.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Không thể gia hạn hợp đồng của người khác.");
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Chỉ hợp đồng đang hoạt động mới được gia hạn.");
        }

        if (newEndDate.isBefore(contract.getEndDate()) || newEndDate.isEqual(contract.getEndDate())) {
            throw new IllegalArgumentException("Thời hạn đề xuất phải sau thời hạn hiện tại.");
        }

        if (requestRepository.existsPendingRequestByContractId(contractId)) {
            throw new IllegalStateException("Đã có yêu cầu gia hạn đang chờ xử lý cho hợp đồng này.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));

        ContractExtensionRequest request = new ContractExtensionRequest();
        request.setContract(contract);
        request.setStudent(student);
        request.setNewEndDate(newEndDate);
        request.setReason(reason);
        request.setStatus(RequestStatus.PENDING);

        return requestRepository.save(request);
    }

    @Override
    public void approveRequest(Long requestId, String adminUsername) {
        ContractExtensionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu không ở trạng thái chờ duyệt.");
        }

        Contract contract = request.getContract();
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Hợp đồng không còn hoạt động.");
        }

        // Cập nhật ngày kết thúc của hợp đồng
        contract.setEndDate(request.getNewEndDate());
        contractRepository.save(contract);

        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(adminUsername);
        requestRepository.save(request);
    }

    @Override
    public void rejectRequest(Long requestId, String rejectReason, String adminUsername) {
        ContractExtensionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu không ở trạng thái chờ duyệt.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectReason(rejectReason);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(adminUsername);
        requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractExtensionRequest> findByStudentId(Long studentId) {
        return requestRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractExtensionRequest> findAll() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractExtensionRequest> findByStatus(RequestStatus status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }
}
