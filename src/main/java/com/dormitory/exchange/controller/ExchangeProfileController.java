package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeReview;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.ExchangeReputationService;
import com.dormitory.exchange.service.ExchangeReviewService;
import com.dormitory.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exchange/users")
public class ExchangeProfileController {

    private final StudentRepository studentRepository;
    private final ExchangeReviewService reviewService;
    private final ExchangeReputationService reputationService;
    private final ExchangeItemService itemService;

    public ExchangeProfileController(StudentRepository studentRepository,
                                     ExchangeReviewService reviewService,
                                     ExchangeReputationService reputationService,
                                     ExchangeItemService itemService) {
        this.studentRepository = studentRepository;
        this.reviewService = reviewService;
        this.reputationService = reputationService;
        this.itemService = itemService;
    }

    @GetMapping("/{userId}")
    public String profile(@PathVariable Long userId, Model model, RedirectAttributes ra) {
        Student user = studentRepository.findById(userId).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/exchange";
        }

        // Đánh giá nhận được, mới nhất trước
        List<ExchangeReview> reviews = new ArrayList<>(reviewService.getByRevieweeId(userId));
        reviews.sort(Comparator.comparing(ExchangeReview::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Tên người đã đánh giá: userId -> họ tên
        Set<Long> reviewerIds = reviews.stream()
                .map(ExchangeReview::getReviewerId).collect(Collectors.toSet());
        Map<Long, String> names = studentRepository.findAllById(reviewerIds).stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));

        // Các tin đang đăng bán (chỉ tin AVAILABLE)
        List<ExchangeItem> activeItems = itemService.getByOwnerId(userId).stream()
                .filter(i -> "AVAILABLE".equals(i.getStatus()))
                .toList();

        model.addAttribute("profileUser", user);
        model.addAttribute("reputation", reputationService.getReputation(userId));
        model.addAttribute("reviews", reviews);
        model.addAttribute("names", names);
        model.addAttribute("activeItems", activeItems);
        return "exchange/profile";
    }
}