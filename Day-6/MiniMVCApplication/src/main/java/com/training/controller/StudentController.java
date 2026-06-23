package com.training.controller;

import com.training.model.Student;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentController {


    @ResponseBody
    @GetMapping
    public String getAllStudents(){
        return "Fetching all students ";
    }

    @ResponseBody
    @GetMapping("search/{id}")
    public String getStudentById(@PathVariable int id){
        return "Fetching student with Id "+id;
    }
    @ResponseBody
    @GetMapping("/{id}/courses/{courseId}")
    public String getStudentCourse(@PathVariable int id, @PathVariable int courseId){
        return "Student id "+ id +" Course Id "+courseId;
    }

    @ResponseBody
    @GetMapping("/search")
    public String searchStudent(@RequestParam int id) {
        return "Searching Student with Id " + id;
    }

    // Student
    @GetMapping("/studentForm")
    public String showForm() {
        return "student-form";
    }

    @PostMapping("/saveStudent")
    public String saveStudent(@ModelAttribute Student student) {

        System.out.println(student.getName());
        System.out.println(student.getAge());

        return "success";
    }

}
