package com.dormitory.controller;

import com.dormitory.entity.Allocation;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;

import com.dormitory.repository.AllocationRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.StudentRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;

@Controller
public class AllocationController {

    private final AllocationRepository allocationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;

    public AllocationController(AllocationRepository allocationRepository, StudentRepository studentRepository, RoomRepository roomRepository) {
        this.allocationRepository = allocationRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/allocations")
    public String allocations(Model model, @RequestParam(value = "error", required = false) String error, HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "no_access";
        }

        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("allocations", allocationRepository.findAllByOrderByIdDesc());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("error", error);

        return "allocations";
    }

    @PostMapping("/allocations/checkin")
    public String checkIn(@RequestParam Integer studentId, @RequestParam Integer roomId, @RequestParam String checkInDate){

        if (allocationRepository.existsByStudentId(studentId)) {
            return "redirect:/allocations?error=This student already has allocation";
        }

        Student student = studentRepository.findById(studentId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        Allocation a = new Allocation();
        a.setStudent(student);
        a.setRoom(room);
        a.setCheckInDate(LocalDate.parse(checkInDate));
        a.setStatus("UNPAID");
        allocationRepository.save(a);

        return "redirect:/allocations";
    }

    @PostMapping("/allocations/paid/{id}")
    public String markPaid(@PathVariable Integer id) {

        Allocation a = allocationRepository.findById(id).orElseThrow();

        if ("PAID".equals(a.getStatus())) {
            return "redirect:/allocations";
        }

        Room room = a.getRoom();

        int occupied = (room.getOccupied() == null) ? 0 : room.getOccupied();
        int capacity = (room.getCapacity() == null) ? 0 : room.getCapacity();

        if (occupied >= capacity) {
            return "redirect:/allocations?error=Room is FULL";
        }

        a.setStatus("PAID");
        allocationRepository.save(a);

        room.setOccupied(occupied + 1);
        room.updateStatus();
        roomRepository.save(room);

        return "redirect:/allocations";
    }

    @PostMapping("/allocations/unpaid/{id}")
    public String markUnpaid(@PathVariable Integer id) {

        Allocation a = allocationRepository.findById(id).orElseThrow();

        if ("UNPAID".equals(a.getStatus())) {
            return "redirect:/allocations";
        }

        Room room = a.getRoom();

        int occupied = (room.getOccupied() == null) ? 0 : room.getOccupied();

        a.setStatus("UNPAID");
        allocationRepository.save(a);

        if (occupied > 0) {
            room.setOccupied(occupied - 1);
            room.updateStatus();
            roomRepository.save(room);
        }

        return "redirect:/allocations";
    }

    @PostMapping("/allocations/delete/{id}")
    public String delete(@PathVariable Integer id) {

        Allocation a = allocationRepository.findById(id).orElseThrow();

        if ("PAID".equals(a.getStatus())) {
            Room room = a.getRoom();
            int occupied = (room.getOccupied() == null) ? 0 : room.getOccupied();
            if (occupied > 0) {
                room.setOccupied(occupied - 1);
                roomRepository.save(room);
            }
        }

        allocationRepository.deleteById(id);
        return "redirect:/allocations";
    }
}
