package com.dormitory.service.impl;

import com.dormitory.entity.Contract;
import com.dormitory.entity.Room;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.repository.ContractRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.service.ContractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository     roomRepository;

    public ContractServiceImpl(ContractRepository contractRepository,
                               RoomRepository roomRepository) {
        this.contractRepository = contractRepository;
        this.roomRepository     = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findActiveContractByStudentId(Long studentId) {
        return contractRepository.findByStudentIdAndStatus(studentId, ContractStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> findByStudentId(Long studentId) {
        return contractRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Kết thúc sớm hợp đồng:
     * 1. Đổi trạng thái → TERMINATED
     * 2. Giảm room.occupied → cập nhật room status
     */
    @Override
    public void terminateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hợp đồng ID: " + contractId));

        if (!contract.isActive()) {
            throw new IllegalStateException("Chỉ có thể kết thúc hợp đồng đang ACTIVE.");
        }

        contract.setStatus(ContractStatus.TERMINATED);
        contractRepository.save(contract);

        // Giảm occupied trong phòng
        Room room = contract.getRoom();
        room.decrementOccupied();
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(ContractStatus status) {
        return contractRepository.countByStatus(status);
    }
}
