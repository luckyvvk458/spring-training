# Spring Framework Training - Day 6

# Learning Objectives

By the end of this session, participants should be able to:

* Understand Request Mapping in Spring MVC
* Use Path Variables to capture URL values
* Handle Form Submission
* Understand @RequestParam
* Understand @ModelAttribute
* Understand Data Binding
* Understand Validation
* Use @Valid and BindingResult
* Build a Mini MVC Application

---

# Session 1: Request Mapping & Path Variables

Duration: 2 Hours

---

## What is Request Mapping?

In a web application, users access different URLs.

Examples:

```text
/student
/student/101
/student/101/courses/10
```

Spring MVC needs to know:

```text
Which URL
       ↓
Which Controller Method
```

This process is called:

```text
Request Mapping
```

---

## Controller

```java
package com.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentController {
}
```

---

## Class Level Mapping

```java
@RequestMapping("/student")
```

Base URL becomes:

```text
/student
```

All methods inside controller will be mapped under:

```text
/student
```

---

## @GetMapping

```java
@GetMapping
@ResponseBody
public String getAllStudents() {

    return "Fetching all students";
}
```

URL:

```text
/student
```

Output:

```text
Fetching all students
```

---

## Path Variables

Many applications pass values inside URL.

Example:

```text
/student/101
```

Here:

```text
101
```

is dynamic.

---

## @PathVariable

```java
@GetMapping("/{id}")
@ResponseBody
public String getStudentById(
        @PathVariable int id) {

    return "Fetching Student : " + id;
}
```

URL:

```text
/student/101
```

Output:

```text
Fetching Student : 101
```

---

## Multiple Path Variables

```java
@GetMapping("/{id}/courses/{courseId}")
@ResponseBody
public String getStudentCourse(
        @PathVariable int id,
        @PathVariable int courseId) {

    return "Student Id : " + id +
            " Course Id : " + courseId;
}
```

URL:

```text
/student/101/courses/10
```

Output:

```text
Student Id : 101 Course Id : 10
```

---

## Internal Flow

```text
Browser

/student/101

       ↓

DispatcherServlet

       ↓

StudentController

       ↓

@PathVariable

       ↓

Method Invocation

       ↓

Response
```

---

## Real World Examples

```text
/products/101

/users/200

/orders/500

/employees/123
```

---

## Assessment Questions

1. What is Request Mapping?
2. What is @GetMapping?
3. What is @RequestMapping?
4. What is a Path Variable?
5. Difference between Query Parameter and Path Variable?

---

# Session 2: Form Handling

Duration: 2 Hours

---

## Problem Statement

Most enterprise applications accept user input.

Examples:

```text
Employee Registration

Student Registration

Login Form

Bank Account Opening Form
```

---

## HTML Form

```html
<form action="/employee/register"
      method="post">

    Name:
    <input type="text" name="name">

    Age:
    <input type="number" name="age">

    <input type="submit" value="Submit">

</form>
```

---

## What Happens Internally?

Browser sends:

```text
name=Vivek
age=30
```

to server.

---

## Using @RequestParam

```java
@PostMapping("/register")
@ResponseBody
public String registerEmployee(
        @RequestParam String name,
        @RequestParam int age) {

    return name + " " + age;
}
```

---

## Problem with @RequestParam

Imagine Employee contains:

```text
name
email
age
department
salary
address
city
state
country
```

Controller becomes:

```java
@PostMapping
public String save(
    @RequestParam String name,
    @RequestParam String email,
    @RequestParam int age,
    @RequestParam String department,
    @RequestParam double salary
)
```

Difficult to maintain.

---

## Employee POJO

```java
public class Employee {

    private String name;
    private String email;
    private int age;

    // getters setters
}
```

---

## @ModelAttribute

```java
@PostMapping("/register")
@ResponseBody
public String registerEmployee(
        @ModelAttribute Employee employee) {

    return employee.toString();
}
```

---

## How @ModelAttribute Works

Spring internally performs:

```java
Employee employee =
        new Employee();

employee.setName("Vivek");
employee.setEmail("vivek@gmail.com");
employee.setAge(30);
```

Then passes object to controller.

---

## Data Binding

Process of converting:

```text
Request Parameters
```

into:

```text
Java Object
```

is called:

```text
Data Binding
```

---

## Internal Flow

```text
HTML Form

      ↓

Request Parameters

      ↓

@ModelAttribute

      ↓

Employee Object

      ↓

Controller
```

---

## Assessment Questions

1. What is Form Handling?
2. What is @RequestParam?
3. What is @ModelAttribute?
4. What is Data Binding?
5. Why use POJO instead of multiple Request Parameters?

---

# Session 3: Validation & Binding

Duration: 2 Hours

---

## Why Validation?

Without validation:

```text
Name = ""

Email = abc

Age = 10
```

Application still accepts data.

This is not correct.

---

## Validation

Validation ensures:

```text
Data is correct

Data is complete

Data follows business rules
```

---

## Employee Class

```java
import javax.validation.constraints.*;

public class Employee {

    @NotBlank(message =
            "Name cannot be empty")
    private String name;

    @Email(message =
            "Invalid Email")
    private String email;

    @Min(value = 18,
            message =
                    "Age must be at least 18")
    private int age;

    @NotBlank(message =
            "Department cannot be empty")
    private String department;
}
```

---

## Validation Annotations

### @NotBlank

Invalid:

```text
null
""
" "
```

---

### @Email

Valid:

```text
vivek@gmail.com
```

Invalid:

```text
vivek
abc
```

---

### @Min

```java
@Min(18)
```

Age must be:

```text
18 or greater
```

---

## @Valid

```java
@PostMapping("/register")
public String registerEmployee(
        @Valid
        @ModelAttribute Employee employee) {

}
```

Triggers validation process.

---

## BindingResult

Validation errors are stored inside:

```java
BindingResult
```

---

## Example

```java
@PostMapping("/register")
@ResponseBody
public String registerEmployee(
        @Valid
        @ModelAttribute Employee employee,
        BindingResult result) {

    if(result.hasErrors()) {

        return result
                .getAllErrors()
                .toString();
    }

    return "Success";
}
```

---

## Internal Flow

```text
Form

      ↓

@ModelAttribute

      ↓

Employee Object

      ↓

@Valid

      ↓

Validation

      ↓

BindingResult

      ↓

Controller
```

---

## Binding vs Validation

### Binding

```text
Request
      ↓
Object
```

### Validation

```text
Object
      ↓
Check Rules
```

---

## Assessment Questions

1. What is Validation?
2. What is @Valid?
3. What is BindingResult?
4. Difference between Binding and Validation?
5. What is @NotBlank?
6. What is @Email?
7. What is @Min?

---

# Session 4: Mini MVC Application

Duration: 2 Hours

---

# Employee Registration System

---

## Problem Statement

Company wants employees to register using a web application.

Application should:

```text
Display Form

       ↓

Accept Employee Details

       ↓

Validate Data

       ↓

Show Success Message
```

---

## Employee.java

```java
package com.training.model;

import javax.validation.constraints.*;

public class Employee {

    @NotBlank(message =
            "Name cannot be empty")
    private String name;

    @Email(message =
            "Invalid Email")
    private String email;

    @Min(value = 18,
            message =
                    "Age must be at least 18")
    private int age;

    @NotBlank(message =
            "Department cannot be empty")
    private String department;

    // getters setters
}
```

---

## EmployeeController.java

```java
@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @GetMapping("/form")
    @ResponseBody
    public String showForm() {

        return """
                <form action="/MiniMVCApplication/employee/register"
                      method="post">

                    Name:
                    <input type="text" name="name"/>

                    Email:
                    <input type="text" name="email"/>

                    Age:
                    <input type="number" name="age"/>

                    Department:
                    <input type="text" name="department"/>

                    <input type="submit"
                           value="Register"/>

                </form>
                """;
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerEmployee(
            @Valid
            @ModelAttribute Employee employee,
            BindingResult result) {

        if(result.hasErrors()) {

            StringBuilder sb =
                    new StringBuilder();

            result.getAllErrors()
                    .forEach(error ->
                            sb.append(
                                    error.getDefaultMessage())
                                    .append("<br>")
                    );

            return sb.toString();
        }

        return "Employee Registered Successfully";
    }
}
```

---

# End of Day Assessment

## Theory

1. What is Request Mapping?
2. What is Path Variable?
3. What is @ModelAttribute?
4. What is Data Binding?
5. What is Validation?
6. What is @Valid?
7. What is BindingResult?
8. Difference between Binding and Validation?
9. Difference between @RequestParam and @ModelAttribute?
10. Explain complete MVC Request Flow.

---

## Coding Assignment 1

Create:

```text
Employee Controller
```

Implement:

```text
Get All Employees

Get Employee By Id

Get Employee Department
```

using Path Variables.

---

## Coding Assignment 2

Create:

```text
Customer Registration Form
```

Fields:

```text
Name
Email
Phone
City
```

Use:

```text
@ModelAttribute
```

---

## Coding Assignment 3

Apply Validation:

```text
@NotBlank
@Email
@Size
```

for Customer Registration.

---

## Coding Assignment 4

Build:

```text
Product Registration Application
```

Features:

```text
Form Handling

Data Binding

Validation

Success Message
```

using complete Spring MVC flow.
