package com.dormitory.controller;

import com.dormitory.entity.Room;
import com.dormitory.repository.RoomRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@Controller
public class RoomController{

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/rooms")
    public String rooms(Model model, HttpSession session){

        if (session.getAttribute("admin") == null) {
            return "no_access";
        }

        model.addAttribute("rooms", roomRepository.findAll());
        return "rooms";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room) {
        room.setOccupied(0);
        room.setStatus("AVAILABLE");
        roomRepository.save(room);

        return "redirect:/rooms";
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Integer id) {
        roomRepository.deleteById(id);

        return "redirect:/rooms";
    }




}

