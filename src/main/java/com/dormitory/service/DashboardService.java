package com.dormitory.service;

/**
 * Service interface cho dữ liệu thống kê dashboard.
 */
public interface DashboardService {

    long getTotalRooms();

    long getTotalStudents();       // Sinh viên đang ở KTX

    long getTotalStudentsAll();    // Tổng tất cả sinh viên trong DB

    int getTotalEmptyPlaces();

    long getTotalActiveContracts();

    long getTotalPendingRegistrations();

    long getTotalUnpaidInvoices();

    long getTotalUnpaidViolations();
}
