package com.dormitory.service.impl;

import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.entity.enums.InvoiceStatus;
import com.dormitory.entity.enums.RegistrationStatus;
import com.dormitory.entity.enums.ViolationStatus;
import com.dormitory.repository.*;
import com.dormitory.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final RoomRepository         roomRepository;
    private final StudentRepository      studentRepository;
    private final ContractRepository     contractRepository;
    private final RegistrationRepository registrationRepository;
    private final InvoiceRepository      invoiceRepository;
    private final ViolationRepository    violationRepository;

    public DashboardServiceImpl(RoomRepository         roomRepository,
                                StudentRepository      studentRepository,
                                ContractRepository     contractRepository,
                                RegistrationRepository registrationRepository,
                                InvoiceRepository      invoiceRepository,
                                ViolationRepository    violationRepository) {
        this.roomRepository         = roomRepository;
        this.studentRepository      = studentRepository;
        this.contractRepository     = contractRepository;
        this.registrationRepository = registrationRepository;
        this.invoiceRepository      = invoiceRepository;
        this.violationRepository    = violationRepository;
    }

    @Override public long getTotalRooms()               { return roomRepository.count(); }
    @Override public long getTotalStudents()            { return studentRepository.countStudentsWithActiveContract(); }
    @Override public long getTotalStudentsAll()         { return studentRepository.count(); }
    @Override public int  getTotalEmptyPlaces()         {
        Integer r = roomRepository.getTotalEmptyPlaces(); return r != null ? r : 0;
    }
    @Override public long getTotalActiveContracts()     { return contractRepository.countByStatus(ContractStatus.ACTIVE); }
    @Override public long getTotalPendingRegistrations(){ return registrationRepository.countByStatus(RegistrationStatus.PENDING); }
    @Override public long getTotalUnpaidInvoices()      { return invoiceRepository.countByStatus(InvoiceStatus.UNPAID); }
    @Override public long getTotalUnpaidViolations()    { return violationRepository.countByStatus(ViolationStatus.UNPAID); }
}
