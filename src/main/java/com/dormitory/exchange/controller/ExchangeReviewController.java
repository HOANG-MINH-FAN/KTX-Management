package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.exchange.entity.ExchangeReview;
import com.dormitory.exchange.service.ExchangeReviewService;
import com.dormitory.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exchange/reviews")
public class ExchangeReviewController {

    private final ExchangeReviewService reviewService;
    private final StudentRepository studentRepository;
    private final ExchangeUserHelper userHelper;

    public ExchangeReviewController(ExchangeReviewService reviewService,
                                    StudentRepository studentRepository,
                                    ExchangeUserHelper userHelper) {
        this.reviewService = reviewService;
        this.studentRepository = studentRepository;
        this.userHelper = userHelper;
    }

    /** Trang đánh giá. Có ?requestId=... thì hiện thêm form đánh giá giao dịch đó. */
    @GetMapping
    public String list(@RequestParam(required = false) Long requestId, Authentication auth, Model model) {
        Student student = userHelper.getStudent(auth);
        if (student == null) return "redirect:/";

        List<ExchangeReview> received = reviewService.getByRevieweeId(student.getId());
        List<ExchangeReview> written = reviewService.getByReviewerId(student.getId());

        Set<Long> ids = new HashSet<>();
        received.forEach(r -> ids.add(r.getReviewerId()));
        written.forEach(r -> ids.add(r.getRevieweeId()));
        Map<Long, String> names = studentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));

        model.addAttribute("received", received);
        model.addAttribute("written", written);
        model.addAttribute("names", names);
        model.addAttribute("average", reviewService.getAverageRating(student.getId()));
        model.addAttribute("requestId", requestId);
        return "exchange/reviews";
    }

    @PostMapping
    public String submit(@RequestParam Long requestId,
                         @RequestParam Integer rating,
                         @RequestParam(required = false) String comment,
                         Authentication auth, RedirectAttributes ra) {
        Student student = userHelper.getStudent(auth);
        if (student == null) return "redirect:/";
        try {
            reviewService.createReview(student.getId(), requestId, rating, comment);
            ra.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/exchange/reviews";
    }
}