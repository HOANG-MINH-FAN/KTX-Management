package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.exchange.dto.ExchangeItemForm;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/exchange/my-items")
public class MyExchangeItemController {

    private final ExchangeItemService service;
    private final UserRepository userRepository;

    public MyExchangeItemController(ExchangeItemService service,
                                    UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    /**
     * DTO hỗ trợ hiển thị dữ liệu và trạng thái hành động trên giao diện "Tin của tôi".
     */
    public static class MyItemView {
        private final ExchangeItem item;
        private final boolean hasRequests;
        private final boolean canDelete;
        private final boolean canEdit;

        public MyItemView(ExchangeItem item, boolean hasRequests, boolean canDelete, boolean canEdit) {
            this.item = item;
            this.hasRequests = hasRequests;
            this.canDelete = canDelete;
            this.canEdit = canEdit;
        }

        public ExchangeItem getItem() { return item; }
        public boolean isHasRequests() { return hasRequests; }
        public boolean isCanDelete() { return canDelete; }
        public boolean isCanEdit() { return canEdit; }
    }

    /**
     * Trang "Tin của tôi" - danh sách tin đăng cá nhân của sinh viên hiện tại.
     */
    @GetMapping
    public String myItems(Authentication authentication, Model model, RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng quản lý tin cá nhân chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        List<ExchangeItem> items = service.getByOwnerId(student.getId());
        List<MyItemView> itemViews = new ArrayList<>();

        long availableCount = 0;
        long closedCount = 0;
        long withRequestsCount = 0;

        for (ExchangeItem item : items) {
            boolean hasReqs = service.hasExchangeRequests(item.getId());
            boolean hasTrans = service.hasAcceptedOrCompletedTransaction(item.getId());
            boolean canDel = !hasReqs && !hasTrans;
            boolean canEdt = !hasTrans;

            if ("AVAILABLE".equalsIgnoreCase(item.getStatus())) availableCount++;
            if ("CLOSED".equalsIgnoreCase(item.getStatus())) closedCount++;
            if (hasReqs) withRequestsCount++;

            itemViews.add(new MyItemView(item, hasReqs, canDel, canEdt));
        }

        model.addAttribute("itemViews", itemViews);
        model.addAttribute("totalCount", items.size());
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("closedCount", closedCount);
        model.addAttribute("withRequestsCount", withRequestsCount);
        model.addAttribute("student", student);

        return "exchange/my-items";
    }

    /**
     * Mở form đăng tin mới.
     */
    @GetMapping("/new")
    public String createForm(Authentication authentication, Model model, RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền đăng tin trao đổi đồ.");
            return "redirect:/exchange";
        }

        if (!model.containsAttribute("item")) {
            ExchangeItemForm form = new ExchangeItemForm();
            form.setExchangeType("SELL");
            form.setItemCondition("NEW");
            model.addAttribute("item", form);
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("student", student);
        return "exchange/form";
    }

    /**
     * Xử lý đăng tin mới (POST + CSRF).
     */
    @PostMapping("/new")
    public String createItem(@Valid @ModelAttribute("item") ExchangeItemForm form,
                             BindingResult bindingResult,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chỉ sinh viên mới có quyền đăng tin.");
            return "redirect:/exchange";
        }

        validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("student", student);
            return "exchange/form";
        }

        try {
            service.createItem(form, student.getId(), image);
            ra.addFlashAttribute("success", "Đăng tin trao đổi thành công! Tin của bạn đang ở trạng thái AVAILABLE.");
            return "redirect:/exchange/my-items";
        } catch (IllegalArgumentException | SecurityException e) {
            model.addAttribute("imageError", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("student", student);
            return "exchange/form";
        } catch (Exception e) {
            model.addAttribute("systemError", "Có lỗi xảy ra trong quá trình lưu tin: " + e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("student", student);
            return "exchange/form";
        }
    }

    /**
     * Mở form sửa tin.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng sửa tin chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        ExchangeItem item = service.getById(id).orElse(null);
        if (item == null) {
            ra.addFlashAttribute("error", "Không tìm thấy tin đăng cần sửa.");
            return "redirect:/exchange/my-items";
        }

        // Kiểm tra quyền sở hữu
        if (!item.getOwnerId().equals(student.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa tin đăng của người khác.");
            return "redirect:/exchange/my-items";
        }

        // Không cho sửa tin đã có giao dịch được chấp nhận hoặc hoàn thành
        if (service.hasAcceptedOrCompletedTransaction(id)) {
            ra.addFlashAttribute("error", "Không thể sửa tin đã có giao dịch được chấp nhận hoặc hoàn thành.");
            return "redirect:/exchange/my-items";
        }

        if (!model.containsAttribute("item")) {
            model.addAttribute("item", ExchangeItemForm.fromEntity(item));
        }
        model.addAttribute("isEdit", true);
        model.addAttribute("itemId", id);
        model.addAttribute("student", student);
        return "exchange/form";
    }

    /**
     * Xử lý cập nhật tin (POST + CSRF).
     */
    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable Long id,
                             @Valid @ModelAttribute("item") ExchangeItemForm form,
                             BindingResult bindingResult,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng sửa tin chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("student", student);
            return "exchange/form";
        }

        try {
            service.updateItem(id, form, student.getId(), image);
            ra.addFlashAttribute("success", "Cập nhật tin đăng thành công!");
            return "redirect:/exchange/my-items";
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            model.addAttribute("imageError", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("student", student);
            return "exchange/form";
        } catch (Exception e) {
            model.addAttribute("systemError", "Không thể cập nhật tin đăng: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("student", student);
            return "exchange/form";
        }
    }

    /**
     * Ẩn / Hiện tin đăng (POST + CSRF). Đổi trạng thái giữa AVAILABLE và CLOSED.
     */
    @PostMapping("/{id}/toggle-hide")
    public String toggleHide(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng này chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        try {
            String newStatus = service.toggleHide(id, student.getId());
            if ("CLOSED".equalsIgnoreCase(newStatus)) {
                ra.addFlashAttribute("success", "Đã ẩn tin đăng thành công. Tin này sẽ không còn xuất hiện trên Chợ đồ dùng.");
            } else {
                ra.addFlashAttribute("success", "Đã mở lại tin đăng thành công. Tin hiện đã sẵn sàng (AVAILABLE) trên Chợ đồ dùng.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/exchange/my-items";
    }

    /**
     * Xóa vĩnh viễn tin đăng (POST + CSRF). Chỉ được xóa khi chưa có yêu cầu giao dịch nào.
     */
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes ra) {
        Student student = getStudent(authentication);
        if (student == null) {
            ra.addFlashAttribute("error", "Chức năng này chỉ dành cho sinh viên.");
            return "redirect:/exchange";
        }

        try {
            service.deleteItem(id, student.getId());
            ra.addFlashAttribute("success", "Đã xóa vĩnh viễn tin đăng thành công.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa tin đăng này.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa tin đăng: " + e.getMessage());
        }

        return "redirect:/exchange/my-items";
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

    private void validateForm(ExchangeItemForm form, BindingResult bindingResult) {
        String type = form.getExchangeType();
        if ("SELL".equalsIgnoreCase(type)) {
            if (form.getPrice() == null) {
                bindingResult.rejectValue("price", "price.required", "Vui lòng nhập giá bán khi chọn hình thức Bán đồ.");
            } else if (form.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("price", "price.invalid", "Giá bán phải lớn hơn 0 VNĐ.");
            }
        } else if ("EXCHANGE".equalsIgnoreCase(type)) {
            if (form.getDesiredItem() == null || form.getDesiredItem().trim().isEmpty()) {
                bindingResult.rejectValue("desiredItem", "desiredItem.required", "Vui lòng nhập món đồ bạn muốn đổi lấy.");
            }
        } else if ("GIVEAWAY".equalsIgnoreCase(type)) {
            if (form.getPrice() != null && form.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                bindingResult.rejectValue("price", "price.notAllowed", "Hình thức Cho tặng (GIVEAWAY) không được đặt giá bán.");
            }
        }

        String condition = form.getItemCondition();
        if (condition != null && !condition.equals("NEW") && !condition.equals("LIKE_NEW") && !condition.equals("USED")) {
            bindingResult.rejectValue("itemCondition", "condition.invalid", "Tình trạng sản phẩm không hợp lệ.");
        }
    }
}
