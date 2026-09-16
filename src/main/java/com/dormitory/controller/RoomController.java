package com.dormitory.controller;

import com.dormitory.entity.Building;
import com.dormitory.entity.Room;
import com.dormitory.entity.enums.RoomStatus;
import com.dormitory.entity.enums.RoomType;
import com.dormitory.service.BuildingService;
import com.dormitory.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * RoomController — CRUD phòng ở (chỉ ADMIN).
 */
@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    private final RoomService     roomService;
    private final BuildingService buildingService;

    public RoomController(RoomService roomService, BuildingService buildingService) {
        this.roomService     = roomService;
        this.buildingService = buildingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms",     roomService.findAll());
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("statuses",  RoomStatus.values());
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("room",      new Room());
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("roomTypes", RoomType.values());
        return "admin/rooms/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Room room = roomService.findById(id).orElse(null);
        if (room == null) {
            ra.addFlashAttribute("error", "Không tìm thấy phòng ID: " + id);
            return "redirect:/admin/rooms";
        }
        model.addAttribute("room",      room);
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("statuses",  RoomStatus.values());
        return "admin/rooms/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam Long    buildingId,
                       @RequestParam String  roomNumber,
                       @RequestParam int     floor,
                       @RequestParam int     capacity,
                       @RequestParam String  roomType,
                       @RequestParam BigDecimal roomFeePerMonth,
                       @RequestParam(required = false) String status,
                       RedirectAttributes ra) {
        Building building = buildingService.findById(buildingId).orElse(null);
        if (building == null) {
            ra.addFlashAttribute("error", "Tòa nhà không hợp lệ.");
            return "redirect:/admin/rooms";
        }

        Room room = (id != null) ? roomService.findById(id).orElse(new Room()) : new Room();
        room.setBuilding(building);
        room.setRoomNumber(roomNumber);
        room.setFloor(floor);
        room.setCapacity(capacity);
        room.setRoomType(RoomType.valueOf(roomType));
        room.setRoomFeePerMonth(roomFeePerMonth);
        if (status != null && id != null) {
            room.setStatus(RoomStatus.valueOf(status));
        }
        roomService.save(room);

        ra.addFlashAttribute("success", "Lưu phòng thành công!");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        roomService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa phòng thành công!");
        return "redirect:/admin/rooms";
    }
}
