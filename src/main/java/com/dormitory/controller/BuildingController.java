package com.dormitory.controller;

import com.dormitory.entity.Building;
import com.dormitory.service.BuildingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * BuildingController — CRUD tòa nhà (chỉ ADMIN).
 */
@Controller
@RequestMapping("/admin/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("buildings", buildingService.findAll());
        return "admin/buildings/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("building", new Building());
        return "admin/buildings/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Building b = buildingService.findById(id).orElse(null);
        if (b == null) {
            ra.addFlashAttribute("error", "Không tìm thấy tòa nhà.");
            return "redirect:/admin/buildings";
        }
        model.addAttribute("building", b);
        return "admin/buildings/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long   id,
                       @RequestParam String  name,
                       @RequestParam(required = false) String address,
                       @RequestParam int     totalFloors,
                       @RequestParam(required = false) String description,
                       RedirectAttributes ra) {

        if (id == null && buildingService.existsByName(name)) {
            ra.addFlashAttribute("error", "Tòa nhà '" + name + "' đã tồn tại.");
            return "redirect:/admin/buildings/new";
        }

        Building b = (id != null) ? buildingService.findById(id).orElse(new Building()) : new Building();
        b.setName(name);
        b.setAddress(address);
        b.setTotalFloors(totalFloors);
        b.setDescription(description);
        buildingService.save(b);

        ra.addFlashAttribute("success", "Lưu tòa nhà thành công!");
        return "redirect:/admin/buildings";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            buildingService.deleteById(id);
            ra.addFlashAttribute("success", "Xóa tòa nhà thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa tòa nhà (có phòng liên kết).");
        }
        return "redirect:/admin/buildings";
    }
}
