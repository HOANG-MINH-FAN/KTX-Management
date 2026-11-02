package com.dormitory.exchange.controller;

import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.service.ExchangeItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/** Controller chỉ phục vụ duyệt, tìm kiếm và xem chi tiết tin trao đổi. */
@Controller
@RequestMapping("/exchange")
public class ExchangeItemController {

    private static final int PAGE_SIZE = 9;
    private final ExchangeItemService service;

    public ExchangeItemController(ExchangeItemService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String exchangeType,
                       @RequestParam(required = false) String itemCondition,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        List<ExchangeItem> available = service.getAvailable();
        String keyword = normalize(q);
        String selectedCategory = clean(category);
        String selectedType = clean(exchangeType);
        String selectedCondition = clean(itemCondition);

        BigDecimal safeMin = minPrice != null && minPrice.signum() >= 0 ? minPrice : null;
        BigDecimal safeMax = maxPrice != null && maxPrice.signum() >= 0 ? maxPrice : null;
        String filterMessage = null;
        if (safeMin != null && safeMax != null && safeMin.compareTo(safeMax) > 0) {
            BigDecimal temp = safeMin;
            safeMin = safeMax;
            safeMax = temp;
            filterMessage = "Khoảng giá đã được tự động sắp xếp lại từ thấp đến cao.";
        } else if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0)) {
            filterMessage = "Giá lọc không được là số âm; điều kiện giá không hợp lệ đã được bỏ qua.";
        }

        final BigDecimal filterMin = safeMin;
        final BigDecimal filterMax = safeMax;
        List<ExchangeItem> filtered = available.stream()
                .filter(item -> keyword.isEmpty() || contains(item.getTitle(), keyword)
                        || contains(item.getDescription(), keyword) || contains(item.getCategory(), keyword))
                .filter(item -> selectedCategory == null || selectedCategory.equals(item.getCategory()))
                .filter(item -> selectedType == null || selectedType.equalsIgnoreCase(item.getExchangeType()))
                .filter(item -> selectedCondition == null || selectedCondition.equalsIgnoreCase(item.getItemCondition()))
                .filter(item -> filterMin == null || (item.getPrice() != null && item.getPrice().compareTo(filterMin) >= 0))
                .filter(item -> filterMax == null || (item.getPrice() != null && item.getPrice().compareTo(filterMax) <= 0))
                .sorted(Comparator.comparing(ExchangeItem::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int total = filtered.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 0), pageCount - 1);
        Pageable pageable = PageRequest.of(currentPage, PAGE_SIZE);
        int from = Math.min(currentPage * PAGE_SIZE, total);
        int to = Math.min(from + PAGE_SIZE, total);
        Page<ExchangeItem> resultPage = new PageImpl<>(filtered.subList(from, to), pageable, total);

        List<String> categories = available.stream().map(ExchangeItem::getCategory)
                .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        model.addAttribute("items", resultPage.getContent());
        model.addAttribute("resultPage", resultPage);
        model.addAttribute("categories", categories);
        model.addAttribute("q", q);
        model.addAttribute("category", selectedCategory);
        model.addAttribute("exchangeType", selectedType);
        model.addAttribute("itemCondition", selectedCondition);
        model.addAttribute("minPrice", safeMin);
        model.addAttribute("maxPrice", safeMax);
        model.addAttribute("filterMessage", filterMessage);
        return "exchange/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        return service.getById(id)
                .filter(item -> "AVAILABLE".equalsIgnoreCase(item.getStatus()))
                .map(item -> {
                    model.addAttribute("item", item);
                    service.getOwnerStudent(item.getOwnerId()).ifPresent(owner -> {
                        model.addAttribute("ownerPhone", owner.getPhone());
                        model.addAttribute("ownerName", owner.getFullName());
                    });
                    return "exchange/detail";
                })
                .orElse("redirect:/exchange");
    }

    /** Giữ tương thích URL đăng tin cũ; phần quản lý thuộc controller của Người 1. */
    @GetMapping("/new")
    public String redirectToNewForm() {
        return "redirect:/exchange/my-items/new";
    }

    private static String clean(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
