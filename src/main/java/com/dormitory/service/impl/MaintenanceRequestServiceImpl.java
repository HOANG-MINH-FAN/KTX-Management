package com.dormitory.service.impl;

import com.dormitory.entity.Admin;
import com.dormitory.entity.Contract;
import com.dormitory.entity.MaintenanceRequest;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;
import com.dormitory.repository.AdminRepository;
import com.dormitory.repository.MaintenanceRequestRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.StudentRepository;
import com.dormitory.service.ContractService;
import com.dormitory.service.MaintenanceRequestService;
import com.dormitory.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaintenanceRequestServiceImpl
        implements MaintenanceRequestService {

    private final MaintenanceRequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final ContractService contractService;
    private final NotificationService notificationService;

    public MaintenanceRequestServiceImpl(
            MaintenanceRequestRepository requestRepository,
            RoomRepository roomRepository,
            StudentRepository studentRepository,
            AdminRepository adminRepository,
            ContractService contractService,
            NotificationService notificationService) {

        this.requestRepository = requestRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.contractService = contractService;
        this.notificationService = notificationService;
    }

    @Override
    public MaintenanceRequest createRequest(
            Long studentId,
            Long roomId,
            String title,
            String description) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sinh viên"));

        Contract contract = contractService
                .findActiveContractByStudentId(studentId)
                .filter(Contract::isCurrentlyValid)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bạn không có hợp đồng ở còn hiệu lực"));

        if (!contract.getRoom().getId().equals(roomId)) {
            throw new RuntimeException(
                    "Bạn chỉ được gửi yêu cầu cho phòng mình đang ở");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phòng"));

        if (title == null || title.isBlank() || title.length() > 150) {
            throw new IllegalArgumentException(
                    "Tiêu đề không được để trống và tối đa 150 ký tự");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Vui lòng mô tả sự cố");
        }

        MaintenanceRequest request = new MaintenanceRequest();
        request.setStudent(student);
        request.setRoom(room);
        request.setTitle(title.trim());
        request.setDescription(description.trim());
        request.setStatus("PENDING");

        MaintenanceRequest savedRequest = requestRepository.save(request);

        // Thông báo cho tất cả Admin khi có yêu cầu sửa chữa mới
        List<Admin> admins = adminRepository.findAll();

        for (Admin admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "MAINTENANCE",
                    "Có yêu cầu sửa chữa mới",
                    "Sinh viên " + student.getFullName()
                            + " vừa gửi yêu cầu sửa chữa: "
                            + title.trim(),
                    "MAINTENANCE",
                    savedRequest.getId()
            );
        }

        return savedRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getRequestsByStudent(Long studentId) {
        return requestRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getRequestsByStatus(String status) {
        if (status == null || status.isBlank()) {
            return getAllRequests();
        }

        if (!List.of("PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED")
                .contains(status)) {
            throw new IllegalArgumentException("Trạng thái lọc không hợp lệ");
        }

        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public MaintenanceRequest updateStatus(
            Long requestId,
            String status,
            String adminNote) {

        if (status == null
                || !List.of("PENDING", "IN_PROGRESS",
                            "COMPLETED", "REJECTED").contains(status)) {
            throw new IllegalArgumentException(
                    "Trạng thái sửa chữa không hợp lệ");
        }

        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy yêu cầu"));

        String oldStatus = request.getStatus();

        // Chỉ cho phép chuyển trạng thái theo đúng quy trình
        boolean allowed =
                ("PENDING".equals(oldStatus)
                        && ("IN_PROGRESS".equals(status)
                            || "REJECTED".equals(status)))
                || ("IN_PROGRESS".equals(oldStatus)
                        && ("COMPLETED".equals(status)
                            || "REJECTED".equals(status)));

        if (!status.equals(oldStatus) && !allowed) {
            throw new IllegalArgumentException(
                    "Không thể chuyển từ trạng thái "
                            + oldStatus + " sang " + status);
        }

        request.setStatus(status);
        request.setAdminNote(adminNote);

        MaintenanceRequest savedRequest = requestRepository.save(request);

        // Chỉ gửi thông báo cho sinh viên khi trạng thái thực sự thay đổi
        if (!status.equals(oldStatus)) {
            String message;

            switch (status) {
                case "IN_PROGRESS":
                    message = "Yêu cầu sửa chữa đang được xử lý.";
                    break;

                case "COMPLETED":
                    message = "Yêu cầu sửa chữa đã hoàn thành.";
                    break;

                case "REJECTED":
                    message = "Yêu cầu sửa chữa đã bị từ chối.";
                    break;

                default:
                    message = "Trạng thái yêu cầu sửa chữa đã được cập nhật.";
            }

            if (adminNote != null && !adminNote.isBlank()) {
                message += " Ghi chú từ quản lý: "
                        + adminNote.trim();
            }

            notificationService.createNotification(
                    request.getStudent().getId(),
                    "MAINTENANCE",
                    "Cập nhật yêu cầu sửa chữa",
                    message,
                    "MAINTENANCE",
                    request.getId()
            );
        }

        return savedRequest;
    }
}