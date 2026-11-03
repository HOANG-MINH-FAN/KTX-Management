package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.exchange.dto.ExchangeRequestView;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.ExchangeRequestService;
import com.dormitory.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/exchange")
public class ExchangeRequestController {

    private static final int HISTORY_PAGE_SIZE = 10;

    private final ExchangeRequestService requestService;
    private final ExchangeItemService itemService;
    private final UserRepository userRepository;

    public ExchangeRequestController(ExchangeRequestService requestService,
                                     ExchangeItemService itemService,
                                     UserRepository userRepository) {
        this.requestService = requestService;
        this.itemService = itemService;
        this.userRepository = userRepository;
    }

    /**
     * Trang "Yêu cầu đã gửi" - hiển thị các yêu cầu mà sinh viên hiện tại đã gửi.
     */
    @GetMapping({"/requests/sent", "/requests-sent"})
    public String sentRequests(@RequestParam(required = false) String status,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng xem yêu cầu giao dịch chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        List<ExchangeRequestView> allSent = requestService.getSentRequestViews(student.getId(), null);
        List<ExchangeRequestView> filteredSent = (status != null && !status.isBlank())
                ? requestService.getSentRequestViews(student.getId(), status)
                : allSent;

        long pendingCount = allSent.stream()
                .filter(v -> ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(v.getStatus())).count();
        long acceptedCount = allSent.stream()
                .filter(v -> ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(v.getStatus())).count();
        long completedCount = allSent.stream()
                .filter(v -> ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(v.getStatus())).count();
        long closedCount = allSent.stream()
                .filter(v -> ExchangeRequest.STATUS_REJECTED.equalsIgnoreCase(v.getStatus())
                        || ExchangeRequest.STATUS_CANCELLED.equalsIgnoreCase(v.getStatus())).count();

        model.addAttribute("requests", filteredSent);
        model.addAttribute("selectedStatus", status != null ? status.trim().toUpperCase() : "");
        model.addAttribute("totalCount", allSent.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("closedCount", closedCount);
        model.addAttribute("student", student);

        return "exchange/requests-sent";
    }

    /**
     * Trang "Yêu cầu nhận được" - chỉ hiển thị yêu cầu gửi đến các tin đăng của sinh viên hiện tại.
     */
    @GetMapping({"/requests/received", "/requests-received"})
    public String receivedRequests(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long itemId,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng quản lý yêu cầu nhận được chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        List<ExchangeRequestView> allReceived = requestService.getReceivedRequestViews(student.getId(), null, null);
        List<ExchangeRequestView> filteredReceived = requestService.getReceivedRequestViews(student.getId(), status, itemId);
        List<ExchangeItem> myItems = itemService.getByOwnerId(student.getId());

        long pendingCount = allReceived.stream()
                .filter(v -> ExchangeRequest.STATUS_PENDING.equalsIgnoreCase(v.getStatus())).count();
        long acceptedCount = allReceived.stream()
                .filter(v -> ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(v.getStatus())).count();
        long completedCount = allReceived.stream()
                .filter(v -> ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(v.getStatus())).count();
        long closedCount = allReceived.stream()
                .filter(v -> ExchangeRequest.STATUS_REJECTED.equalsIgnoreCase(v.getStatus())
                        || ExchangeRequest.STATUS_CANCELLED.equalsIgnoreCase(v.getStatus())).count();

        model.addAttribute("requests", filteredReceived);
        model.addAttribute("myItems", myItems);
        model.addAttribute("selectedStatus", status != null ? status.trim().toUpperCase() : "");
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("totalCount", allReceived.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("closedCount", closedCount);
        model.addAttribute("student", student);

        return "exchange/requests-received";
    }

    /**
     * Trang "Lịch sử giao dịch" - hiển thị các giao dịch ACCEPTED, REJECTED, CANCELLED, COMPLETED
     * mà sinh viên hiện tại tham gia (với vai trò Chủ tin đăng hoặc Người gửi yêu cầu).
     */
    @GetMapping({"/history", "/requests/history"})
    public String history(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String role,
                          @RequestParam(defaultValue = "0") int page,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng lịch sử giao dịch chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        Page<ExchangeRequestView> allHistoryPage = requestService.getHistoryViews(
                student.getId(), null, null, 0, Integer.MAX_VALUE);
        List<ExchangeRequestView> allHistory = allHistoryPage.getContent();

        Page<ExchangeRequestView> historyPage = requestService.getHistoryViews(
                student.getId(), status, role, page, HISTORY_PAGE_SIZE);

        long completedCount = allHistory.stream()
                .filter(v -> ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(v.getStatus())).count();
        long acceptedCount = allHistory.stream()
                .filter(v -> ExchangeRequest.STATUS_ACCEPTED.equalsIgnoreCase(v.getStatus())).count();
        long rejectedCount = allHistory.stream()
                .filter(v -> ExchangeRequest.STATUS_REJECTED.equalsIgnoreCase(v.getStatus())).count();
        long cancelledCount = allHistory.stream()
                .filter(v -> ExchangeRequest.STATUS_CANCELLED.equalsIgnoreCase(v.getStatus())).count();

        model.addAttribute("historyList", historyPage.getContent());
        model.addAttribute("historyPage", historyPage);
        model.addAttribute("selectedStatus", status != null ? status.trim().toUpperCase() : "");
        model.addAttribute("selectedRole", role != null ? role.trim().toUpperCase() : "");
        model.addAttribute("totalCount", allHistory.size());
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("student", student);

        return "exchange/history";
    }

    /**
     * Sinh viên gửi yêu cầu giao dịch đối với một tin đăng (POST + CSRF).
     */
    @PostMapping({"/{itemId}/request", "/requests"})
    public String sendRequest(@PathVariable(required = false) Long itemId,
                              @RequestParam(value = "itemId", required = false) Long formItemId,
                              @RequestParam(value = "transactionType", required = false) String transactionType,
                              @RequestParam(value = "offeredItem", required = false) String offeredItem,
                              @RequestParam(value = "message", required = false) String message,
                              Authentication authentication,
                              RedirectAttributes ra) {
        Long targetItemId = itemId != null ? itemId : formItemId;
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền gửi yêu cầu giao dịch.");
            return targetItemId != null ? "redirect:/exchange/" + targetItemId : "redirect:/exchange";
        }

        if (targetItemId == null) {
            ra.addFlashAttribute("error", "Không xác định được tin đăng cần gửi yêu cầu.");
            return "redirect:/exchange";
        }

        try {
            requestService.createRequest(targetItemId, student.getId(), transactionType, offeredItem, message);
            ra.addFlashAttribute("success", "Đã gửi yêu cầu giao dịch thành công! Bạn có thể theo dõi trạng thái tại đây.");
            return "redirect:/exchange/requests/sent";
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/exchange/" + targetItemId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể gửi yêu cầu giao dịch: " + e.getMessage());
            return "redirect:/exchange/" + targetItemId;
        }
    }

    /**
     * Chủ tin chấp nhận yêu cầu giao dịch (POST + CSRF).
     */
    @PostMapping("/requests/{id}/accept")
    public String acceptRequest(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền xử lý yêu cầu giao dịch.");
            return "redirect:/exchange";
        }

        try {
            requestService.acceptRequest(id, student.getId());
            ra.addFlashAttribute("success",
                    "Đã chấp nhận yêu cầu giao dịch! Tin đăng đã chuyển sang trạng thái Đang giao dịch và các yêu cầu chờ khác đã được đóng tự động.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi chấp nhận yêu cầu: " + e.getMessage());
        }

        return "redirect:/exchange/requests/received";
    }

    /**
     * Chủ tin từ chối yêu cầu giao dịch (POST + CSRF).
     */
    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(value = "rejectReason", required = false) String rejectReason,
                                Authentication authentication,
                                RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền xử lý yêu cầu giao dịch.");
            return "redirect:/exchange";
        }

        try {
            requestService.rejectRequest(id, student.getId(), rejectReason);
            ra.addFlashAttribute("success", "Đã từ chối yêu cầu giao dịch.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi từ chối yêu cầu: " + e.getMessage());
        }

        return "redirect:/exchange/requests/received";
    }

    /**
     * Người gửi hủy yêu cầu giao dịch khi đang ở trạng thái PENDING (POST + CSRF).
     */
    @PostMapping("/requests/{id}/cancel")
    public String cancelRequest(@PathVariable Long id,
                                @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                Authentication authentication,
                                RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền hủy yêu cầu giao dịch.");
            return "redirect:/exchange";
        }

        try {
            requestService.cancelRequest(id, student.getId());
            ra.addFlashAttribute("success", "Đã hủy yêu cầu giao dịch thành công.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi hủy yêu cầu: " + e.getMessage());
        }

        return "redirect:" + sanitizeReturnUrl(returnUrl, "/exchange/requests/sent");
    }

    /**
     * Xác nhận hoàn thành giao dịch từ phía chủ tin hoặc người gửi (POST + CSRF).
     */
    @PostMapping("/requests/{id}/complete")
    public String confirmComplete(@PathVariable Long id,
                                  @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                  Authentication authentication,
                                  RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền xác nhận hoàn thành giao dịch.");
            return "redirect:/exchange";
        }

        try {
            ExchangeRequest updated = requestService.confirmComplete(id, student.getId());
            if (ExchangeRequest.STATUS_COMPLETED.equalsIgnoreCase(updated.getStatus())) {
                ra.addFlashAttribute("success",
                        "Giao dịch đã được cả hai bên xác nhận và chuyển sang trạng thái ĐÃ HOÀN THÀNH!");
            } else {
                ra.addFlashAttribute("success",
                        "Đã ghi nhận xác nhận của bạn! Giao dịch sẽ chuyển sang ĐÃ HOÀN THÀNH ngay khi bên còn lại cũng xác nhận.");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xác nhận hoàn thành giao dịch: " + e.getMessage());
        }

        return "redirect:" + sanitizeReturnUrl(returnUrl, "/exchange/history");
    }

    private Student getStudent(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user instanceof Student) {
            return (Student) user;
        }
        if (user != null && user.isStudent()) {
            return (Student) user;
        }
        return null;
    }

    private String sanitizeReturnUrl(String returnUrl, String defaultPath) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return defaultPath;
        }
        String trimmed = returnUrl.trim();
        if (trimmed.equals("/exchange/requests/sent")
                || trimmed.equals("/exchange/requests/received")
                || trimmed.equals("/exchange/history")
                || trimmed.matches("^/exchange/\\d+$")) {
            return trimmed;
        }
        return defaultPath;
    }
}

