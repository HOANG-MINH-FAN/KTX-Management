package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeReportRepository extends JpaRepository<ExchangeReport, Long> {

    List<ExchangeReport> findAllByOrderByCreatedAtDesc();

    boolean existsByItemIdAndReporterIdAndStatus(Long itemId, Long reporterId, String status);

    long countByStatus(String status);
}