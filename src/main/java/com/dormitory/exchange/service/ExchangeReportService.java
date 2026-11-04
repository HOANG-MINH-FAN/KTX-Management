package com.dormitory.exchange.service;

import com.dormitory.entity.Student;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeReport;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeReportRepository;
import com.dormitory.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeReportService {

    public static final List<String> REASONS = List.of(
            "Lừa đảo / thông tin sai sự thật",
            "Nội dung không phù hợp",
            "Đồ cấm / vi phạm nội quy KTX",
            "Tin trùng lặp / spam",
            "Lý do khác");

    private final ExchangeReportRepository reportRepository;
    private final ExchangeItemRepository itemRepository;
    private final StudentRepository studentRepository;

    public ExchangeReportService(ExchangeReportRepository reportRepository,
                                 ExchangeItemRepository itemRepository,
                                 StudentRepository studentRepository) {
        this.reportRepository = reportRepository;
        this.itemRepository = itemRepository;
        this.studentRepository = studentRepository;
    }

    /** Dòng hiển thị cho trang admin: báo cáo + tên tin + tên người báo cáo. */
    public static class ReportView {
        private final ExchangeReport report;
        private final String itemTitle;
        private final String reporterName;

        public ReportView(ExchangeReport report, String itemTitle, String reporterName) {
            this.report = report;
            this.itemTitle = itemTitle;
            this.reporterName = reporterName;
        }

        public ExchangeReport getReport() { return report; }
        public String getItemTitle() { return itemTitle; }
        public String getReporterName() { return reporterName; }
    }

    public ExchangeReport createReport(Long reporterId, Long itemId, String reason, String description) {
        ExchangeItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Tin đăng không tồn tại."));
        if (item.getOwnerId().equals(reporterId)) {
            throw new IllegalArgumentException("Bạn không thể báo cáo tin của chính mình.");
        }
        if (reason == null || !REASONS.contains(reason)) {
            throw new IllegalArgumentException("Vui lòng chọn lý do báo cáo.");
        }
        if (reportRepository.existsByItemIdAndReporterIdAndStatus(itemId, reporterId, "PENDING")) {
            throw new IllegalArgumentException("Bạn đã báo cáo tin này và đang chờ xử lý.");
        }
        ExchangeReport report = new ExchangeReport();
        report.setItemId(itemId);
        report.setReporterId(reporterId);
        report.setReason(reason);
        report.setDescription(description == null ? null : description.trim());
        return reportRepository.save(report);
    }

    public List<ReportView> getAllViews() {
        List<ReportView> views = new ArrayList<>();
        for (ExchangeReport r : reportRepository.findAllByOrderByCreatedAtDesc()) {
            String title = itemRepository.findById(r.getItemId())
                    .map(ExchangeItem::getTitle).orElse("(tin đã bị xóa)");
            String name = studentRepository.findById(r.getReporterId())
                    .map(Student::getFullName).orElse("?");
            views.add(new ReportView(r, title, name));
        }
        return views;
    }

    public long countPending() {
        return reportRepository.countByStatus("PENDING");
    }

    @Transactional
    public void markResolved(Long reportId) {
        ExchangeReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo."));
        report.setStatus("RESOLVED");
        reportRepository.save(report);
    }

    /** Admin ẩn tin vi phạm (CLOSED) và đánh dấu báo cáo đã xử lý. */
    @Transactional
    public void hideItemAndResolve(Long reportId) {
        ExchangeReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo."));
        itemRepository.findById(report.getItemId()).ifPresent(item -> {
            item.setStatus("CLOSED");
            itemRepository.save(item);
        });
        report.setStatus("RESOLVED");
        reportRepository.save(report);
    }
}