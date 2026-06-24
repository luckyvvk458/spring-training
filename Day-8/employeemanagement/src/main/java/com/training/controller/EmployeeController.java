package com.training.controller;

import com.training.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

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
            (@Valid @ModelAttribute Employee employee, BindingResult result) {

        if(result.hasErrors()){
            return result.getFieldErrors().stream()
                    .map(error -> error.getField()+" : "+error.getDefaultMessage())
                    .collect(Collectors.joining());
        }

        return "Saving Employee Details" +
                "Id: " + employee.getId() +
                " Name: " + employee.getName() +
                " Salary: " + employee.getSalary() +
                " City: " + employee.getCity() +
                " Department: " + employee.getDepartment();
    }

    @PostMapping("/saveReqBody")
    @ResponseBody
    public String registerEmployeeUsingReqBody
            (@Valid @RequestBody Employee employee, BindingResult result) {

        if(result.hasErrors()){
            return result.getFieldErrors().stream()
                    .map(error -> error.getField()+" : "+error.getDefaultMessage())
                    .collect(Collectors.joining());
        }

        return "Saving Employee Details" +
                "Id: " + employee.getId() +
                " Name: " + employee.getName() +
                " Salary: " + employee.getSalary() +
                " City: " + employee.getCity() +
                " Department: " + employee.getDepartment();
    }

    @GetMapping("/empForm")
    public String getEmployeeForm(){
        System.out.println(
                "Inside emp form and before jsp call");
        return "emp-form";
    }

    @PostMapping("/saveForm")
    public String saveForm(
            @Valid @ModelAttribute Employee employee,
            BindingResult result){
        if(result.hasErrors()){
            return result.getFieldErrors().stream()
                    .map(errors-> errors.getField()+" : "
                            +errors.getDefaultMessage())
                    .collect(Collectors.joining());
        }

        return "success";
    }








    @GetMapping("/form")
    public String showForm(){
        return "employee-form";
    }

//    @PostMapping("/saveForm")
//    public String saveEmployee(@Valid @ModelAttribute Employee employee, BindingResult result){
//        if(result.hasErrors()){
//            return result.getFieldErrors()
//                    .stream()
//                    .map(errors -> errors.getField()+" :" + errors.getDefaultMessage())
//                    .collect(Collectors.joining());
//        }
//        return "success";
//    }


}
