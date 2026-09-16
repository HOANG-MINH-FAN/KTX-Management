package com.dormitory.service.impl;

import com.dormitory.entity.*;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.entity.enums.RegistrationStatus;
import com.dormitory.repository.ContractRepository;
import com.dormitory.repository.RegistrationRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Implementation của RegistrationService.
 *
 * Nghiệp vụ quan trọng: approveRegistration()
 *   1. Đổi status đơn → APPROVED
 *   2. Tạo Contract mới từ thông tin đơn
 *   3. Tăng room.occupied → cập nhật room.status
 *   4. Lưu tất cả trong cùng một @Transactional
 * ============================================================
 */
@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ContractRepository     contractRepository;
    private final StudentRepository      studentRepository;
    private final RoomRepository         roomRepository;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   ContractRepository     contractRepository,
                                   StudentRepository      studentRepository,
                                   RoomRepository         roomRepository) {
        this.registrationRepository = registrationRepository;
        this.contractRepository     = contractRepository;
        this.studentRepository      = studentRepository;
        this.roomRepository         = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findAll() {
        return registrationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Registration> findById(Long id) {
        return registrationRepository.findById(id);
    }

    /**
     * Sinh viên gửi đơn đăng ký phòng mới.
     * Ràng buộc: Không được gửi nếu đang có đơn PENDING hoặc đang có hợp đồng ACTIVE.
     */
    @Override
    public Registration submitRegistration(Long studentId, Long roomId,
                                           String desiredStartDate, String note) {
        // Kiểm tra sinh viên tồn tại
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên ID: " + studentId));

        // Kiểm tra không có đơn PENDING
        if (registrationRepository.existsByStudentIdAndStatus(studentId, RegistrationStatus.PENDING)) {
            throw new IllegalStateException("Bạn đã có một đơn đăng ký đang chờ duyệt.");
        }

        // Kiểm tra không có hợp đồng ACTIVE
        if (contractRepository.existsByStudentIdAndStatus(studentId, ContractStatus.ACTIVE)) {
            throw new IllegalStateException("Bạn đang có hợp đồng còn hiệu lực, không thể đăng ký thêm.");
        }

        // Kiểm tra phòng tồn tại và còn chỗ
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ID: " + roomId));

        if (!room.isAvailableForOccupancy()) {
            throw new IllegalStateException("Phòng " + room.getRoomNumber() + " không còn chỗ trống hoặc đang bảo trì.");
        }

        // Tạo đơn đăng ký
        Registration reg = new Registration(student, room, LocalDate.parse(desiredStartDate));
        reg.setNote(note);

        return registrationRepository.save(reg);
    }

    /**
     * Admin duyệt đơn đăng ký.
     *
     * Nghiệp vụ phức tạp — thực hiện trong một @Transactional:
     * 1. Đổi trạng thái đơn → APPROVED
     * 2. Tạo Contract mới
     * 3. Tăng room.occupied → cập nhật trạng thái phòng
     */
    @Override
    public void approveRegistration(Long registrationId, String adminNote) {
        // Bước 1: Lấy và kiểm tra đơn
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ID: " + registrationId));

        if (!reg.isPending()) {
            throw new IllegalStateException("Chỉ có thể duyệt đơn ở trạng thái PENDING.");
        }

        // Bước 2: Cập nhật đơn → APPROVED
        reg.setStatus(RegistrationStatus.APPROVED);
        reg.setAdminNote(adminNote);
        registrationRepository.save(reg);

        // Bước 3: Tạo Contract tự động
        Room    room    = reg.getRoom();
        Student student = reg.getStudent();

        Contract contract = new Contract(
            student,
            room,
            reg.getDesiredStartDate(),
            reg.getDesiredStartDate().plusMonths(6), // Mặc định 6 tháng
            room.getRoomFeePerMonth()                // Snapshot giá phòng hiện tại
        );
        contract.setRegistration(reg);
        contractRepository.save(contract);

        // Bước 4: Tăng occupied trong phòng và cập nhật trạng thái
        room.incrementOccupied(); // Method có kiểm tra đầy/bảo trì
        roomRepository.save(room);
    }

    /**
     * Admin từ chối đơn đăng ký.
     */
    @Override
    public void rejectRegistration(Long registrationId, String adminNote) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ID: " + registrationId));

        if (!reg.isPending()) {
            throw new IllegalStateException("Chỉ có thể từ chối đơn ở trạng thái PENDING.");
        }

        reg.setStatus(RegistrationStatus.REJECTED);
        reg.setAdminNote(adminNote);
        registrationRepository.save(reg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findByStatus(RegistrationStatus status) {
        return registrationRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findByStudentId(Long studentId) {
        return registrationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingRegistration(Long studentId) {
        return registrationRepository.existsByStudentIdAndStatus(studentId, RegistrationStatus.PENDING);
    }
}
