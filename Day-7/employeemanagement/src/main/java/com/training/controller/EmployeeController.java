package com.training.controller;

import com.training.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @GetMapping
    @ResponseBody
    public String getAllEmployees() {
        return "Fetching All Employees";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public String getEmployeeById(@PathVariable int id) {
        return "Fetching Employee with id " + id;
    }

    @GetMapping("/getEmployee")
    @ResponseBody
    public String getEmployeeByIdWithReqBody
            (@RequestParam int id) {
        return "Fetching employee by id using request body " +
                "for this " + id;
    }

    @PostMapping("/save")
    @ResponseBody
    public String registerEmployee
            ( @RequestBody Employee employee) {
        return "Saving Employee Details" +
                "Id: " + employee.getId() +
                " Name: " + employee.getName() +
                " Salary: " + employee.getSalary() +
                " City: " + employee.getCity() +
                " Department: " + employee.getDepartment();
    }


}
