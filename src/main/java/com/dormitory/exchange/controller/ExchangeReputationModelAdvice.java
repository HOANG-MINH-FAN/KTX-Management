package com.dormitory.exchange.controller;

import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.ExchangeReputationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thêm điểm uy tín của chủ tin vào model của trang chi tiết (/exchange/{id})
 * mà không cần sửa ExchangeItemController của Người 2.
 */
@ControllerAdvice(assignableTypes = ExchangeItemController.class)
public class ExchangeReputationModelAdvice {

    private static final Pattern DETAIL_URI = Pattern.compile("^/exchange/(\\d+)$");

    private final ExchangeItemService itemService;
    private final ExchangeReputationService reputationService;

    public ExchangeReputationModelAdvice(ExchangeItemService itemService,
                                         ExchangeReputationService reputationService) {
        this.itemService = itemService;
        this.reputationService = reputationService;
    }

    @ModelAttribute
    public void addOwnerReputation(HttpServletRequest request, Model model) {
        Matcher matcher = DETAIL_URI.matcher(request.getRequestURI());
        if (!matcher.matches()) return;          // chỉ chạy cho trang chi tiết
        try {
            Long itemId = Long.parseLong(matcher.group(1));
            itemService.getById(itemId).ifPresent(item ->
                    model.addAttribute("ownerReputation",
                            reputationService.getReputation(item.getOwnerId())));
        } catch (NumberFormatException ignored) {
        }
    }
}