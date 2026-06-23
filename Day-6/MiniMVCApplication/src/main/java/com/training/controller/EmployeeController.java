package com.training.controller;

import com.training.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @GetMapping("/form")
    @ResponseBody
    public String showForm() {

        return """
                <html>
                <body>

                <h2>Employee Registration Form</h2>

                <form action="/MiniMVCApplication-1.0-SNAPSHOT/employee/register"
                      method="post">

                    Employee Name:
                    <input type="text" name="name"/>

                    <br><br>

                    Email:
                    <input type="text" name="email"/>

                    <br><br>

                    Age:
                    <input type="number" name="age"/>

                    <br><br>

                    Department:
                    <input type="text" name="department"/>

                    <br><br>

                    Salary:
                    <input type="number" name="salary"/>

                    <br><br>

                    <input type="submit"
                           value="Register Employee"/>

                </form>

                </body>
                </html>
                """;
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerEmployee(
            @Valid @ModelAttribute Employee employee,
            BindingResult result) {

        if(result.hasErrors()) {

            StringBuilder errors =
                    new StringBuilder();

            result.getAllErrors()
                    .forEach(error ->
                            errors.append(error.getDefaultMessage())
                                    .append("<br>")
                    );

            return errors.toString();
        }

        return """
                <h2>Employee Registered Successfully</h2>
                <br>
                """ + employee;
    }
}