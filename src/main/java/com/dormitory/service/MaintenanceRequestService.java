package com.dormitory.service;

import com.dormitory.entity.MaintenanceRequest;
import java.util.List;

public interface MaintenanceRequestService {

    MaintenanceRequest createRequest(
            Long studentId,
            Long roomId,
            String title,
            String description
    );

    List<MaintenanceRequest> getRequestsByStudent(Long studentId);

    List<MaintenanceRequest> getAllRequests();

    // Lấy yêu cầu theo trạng thái
    List<MaintenanceRequest> getRequestsByStatus(String status);

    MaintenanceRequest updateStatus(
            Long requestId,
            String status,
            String adminNote
    );
}

