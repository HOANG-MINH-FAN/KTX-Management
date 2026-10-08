package com.dormitory.service.impl;

import com.dormitory.entity.*;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.entity.enums.RegistrationStatus;
import com.dormitory.repository.ContractRepository;
import com.dormitory.repository.RegistrationRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.RegistrationService;
import com.dormitory.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    public RegistrationServiceImpl(
            RegistrationRepository registrationRepository,
            ContractRepository contractRepository,
            StudentRepository studentRepository,
            RoomRepository roomRepository,
            NotificationService notificationService) {

        this.registrationRepository = registrationRepository;
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
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

    @Override
    public Registration submitRegistration(
            Long studentId, Long roomId,
            String desiredStartDate, String note) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sinh viên ID: " + studentId));

        if (registrationRepository.existsByStudentIdAndStatus(
                studentId, RegistrationStatus.PENDING)) {
            throw new IllegalStateException(
                    "Bạn đã có một đơn đăng ký đang chờ duyệt.");
        }

        if (contractRepository.existsByStudentIdAndStatus(
                studentId, ContractStatus.ACTIVE)) {
            throw new IllegalStateException(
                    "Bạn đang có hợp đồng còn hiệu lực, không thể đăng ký thêm.");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy phòng ID: " + roomId));

        if (!room.isAvailableForOccupancy()) {
            throw new IllegalStateException(
                    "Phòng " + room.getRoomNumber()
                    + " không còn chỗ trống hoặc đang bảo trì.");
        }

        Registration reg = new Registration(
                student, room, LocalDate.parse(desiredStartDate));
        reg.setNote(note);

        return registrationRepository.save(reg);
    }

    @Override
    public void approveRegistration(Long registrationId, String adminNote) {

        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn ID: " + registrationId));

        if (!reg.isPending()) {
            throw new IllegalStateException(
                    "Chỉ có thể duyệt đơn ở trạng thái PENDING.");
        }

        reg.setStatus(RegistrationStatus.APPROVED);
        reg.setAdminNote(adminNote);
        registrationRepository.save(reg);

        Room room = reg.getRoom();
        Student student = reg.getStudent();

        Contract contract = new Contract(
                student,
                room,
                reg.getDesiredStartDate(),
                reg.getDesiredStartDate().plusMonths(6),
                room.getRoomFeePerMonth()
        );

        contract.setRegistration(reg);
        contractRepository.save(contract);

        room.incrementOccupied();
        roomRepository.save(room);

        // Gửi thông báo khi đơn được duyệt
        notificationService.createNotification(
                student.getId(),
                "REGISTRATION",
                "Đăng ký phòng được duyệt",
                "Đơn đăng ký phòng " + room.getRoomNumber()
                        + " của bạn đã được duyệt.",
                "REGISTRATION",
                reg.getId()
        );
    }

    @Override
    public void rejectRegistration(Long registrationId, String adminNote) {

        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn ID: " + registrationId));

        if (!reg.isPending()) {
            throw new IllegalStateException(
                    "Chỉ có thể từ chối đơn ở trạng thái PENDING.");
        }

        reg.setStatus(RegistrationStatus.REJECTED);
        reg.setAdminNote(adminNote);
        registrationRepository.save(reg);

        // Gửi thông báo khi đơn bị từ chối
        notificationService.createNotification(
                reg.getStudent().getId(),
                "REGISTRATION",
                "Đăng ký phòng bị từ chối",
                "Đơn đăng ký phòng "
                        + reg.getRoom().getRoomNumber()
                        + " của bạn đã bị từ chối."
                        + (adminNote != null && !adminNote.trim().isEmpty()
                        ? " Lý do: " + adminNote : ""),
                "REGISTRATION",
                reg.getId()
        );
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
        return registrationRepository.existsByStudentIdAndStatus(
                studentId, RegistrationStatus.PENDING);
    }
}

