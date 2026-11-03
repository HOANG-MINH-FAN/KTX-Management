package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.service.ExchangeRequestService;
import com.dormitory.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bổ sung thông tin sinh viên đăng nhập và trạng thái yêu cầu giao dịch hiện tại
 * cho trang chi tiết sản phẩm mà không cần sửa trực tiếp ExchangeItemController của Người 2.
 */
@ControllerAdvice(assignableTypes = ExchangeItemController.class)
public class ExchangeDetailModelAdvice {

    private static final Pattern DETAIL_URI_PATTERN = Pattern.compile("^/exchange/(\\d+)$");

    private final UserRepository userRepository;
    private final ExchangeRequestService requestService;

    public ExchangeDetailModelAdvice(UserRepository userRepository,
                                     ExchangeRequestService requestService) {
        this.userRepository = userRepository;
        this.requestService = requestService;
    }

    @ModelAttribute
    public void populateExchangeDetailAttributes(Authentication authentication,
                                                 HttpServletRequest request,
                                                 Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (!(user instanceof Student student)) {
            return;
        }

        model.addAttribute("currentStudent", student);

        if (request != null && request.getRequestURI() != null) {
            Matcher matcher = DETAIL_URI_PATTERN.matcher(request.getRequestURI());
            if (matcher.matches()) {
                try {
                    Long itemId = Long.parseLong(matcher.group(1));
                    ExchangeRequest existingRequest = requestService
                            .getRequestByItemAndRequester(itemId, student.getId())
                            .orElse(null);
                    model.addAttribute("existingRequest", existingRequest);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}

