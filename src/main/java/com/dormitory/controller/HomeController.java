package com.dormitory.controller;

import com.dormitory.repository.AllocationRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.StudentRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController{

    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;

    public HomeController(RoomRepository roomRepository, StudentRepository studentRepository, AllocationRepository allocationRepository){
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        long totalRooms = roomRepository.count();
        long totalStudents = studentRepository.count();
        Integer emptyPlaces = roomRepository.getTotalEmptyPlaces();
        long unpaid = allocationRepository.countByStatus("UNPAID");

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("emptyPlaces", emptyPlaces);
        model.addAttribute("unpaid", unpaid);

        return "home";
    }
}
