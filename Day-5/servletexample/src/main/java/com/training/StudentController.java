package com.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StudentController {
    @Autowired
    StudentService service;

    @GetMapping("/student")
    @ResponseBody
    public Student getStudent() {
        return service.getStudent();
    }

    @PostMapping("/student")
    @ResponseBody
    public String saveStudent(Student student) {
        service.saveStudent(student);
        return "Student Saved";
    }
}