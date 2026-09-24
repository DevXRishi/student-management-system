package com.rishi.student_management.controller;

import com.rishi.student_management.dto.StudentResponse;
import com.rishi.student_management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private StudentService service;

    @GetMapping("/")
    public String home(Model model) {

        List<StudentResponse> students =
                service.getAllStudents(0, 100, "id", "asc")
                        .getContent();

        model.addAttribute("students", students);

        return "index";
    }
}