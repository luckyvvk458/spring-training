# Employee Management Application - Spring MVC Notes

## Objective

Build an Employee Management application incrementally while learning Spring MVC concepts.

The focus is understanding how Spring MVC handles requests, binds data, performs validation, and communicates between Controller and View.

---

# Phase 1: Project Setup

## Required Dependencies

Initially start with minimal dependencies:

* spring-webmvc
* javax.servlet-api

Example:

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.3.x</version>
</dependency>

<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
```

---

## Initial Project Structure

```text
src
 └── main
      ├── java
      │     └── com.training.controller
      └── webapp
```

---

# Phase 2: First Controller

Create a simple controller.

```java
@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @GetMapping
    @ResponseBody
    public String getAllEmployees() {
        return "Fetching all employees";
    }
}
```

---

## Concepts Introduced

### @Controller

Marks a class as a Spring MVC Controller.

### @RequestMapping

Defines base URL mapping.

```text
/employee
```

### @GetMapping

Handles HTTP GET requests.

### @ResponseBody

Returns response directly to browser/Postman.

Without it, Spring treats the return value as a View Name.

---

## Test

```http
GET /employee
```

Expected Output:

```text
Fetching all employees
```

---

# Phase 3: Request Parameters

Retrieve employee using query parameters.

Example URL:

```http
GET /employee?id=101
```

Controller:

```java
@GetMapping(params = "id")
@ResponseBody
public String getEmployee(
        @RequestParam int id) {

    return "Employee Id : " + id;
}
```

---

## Concepts Introduced

### @RequestParam

Extracts values from URL query parameters.

Example:

```http
/employee?id=101
```

Spring extracts:

```java
101
```

and assigns it to:

```java
@RequestParam int id
```

---

## Curious Point

Can parameter names differ?

Yes.

```java
@RequestParam("id")
int employeeId
```

---

# Phase 4: Path Variables

Alternative approach for identifying resources.

URL:

```http
GET /employee/101
```

Controller:

```java
@GetMapping("/{id}")
@ResponseBody
public String getEmployeeById(
        @PathVariable int id) {

    return "Employee Id : " + id;
}
```

---

## Concepts Introduced

### @PathVariable

Extracts values from URL path.

Example:

```http
/employee/101
```

Spring extracts:

```java
101
```

---

## RequestParam vs PathVariable

RequestParam:

```http
/employee?id=101
```

PathVariable:

```http
/employee/101
```

PathVariable is generally preferred when identifying a specific resource.

---

# Phase 5: Creating Employee Using Request Parameters

Controller:

```java
@PostMapping
@ResponseBody
public String saveEmployee(
        @RequestParam int id,
        @RequestParam String name,
        @RequestParam double salary) {

    return "Employee Saved";
}
```

Request:

```http
POST /employee

id=101
name=John
salary=50000
```

---

## Problem

As fields increase:

```java
@RequestParam
@RequestParam
@RequestParam
@RequestParam
@RequestParam
```

method becomes difficult to maintain.

---

# Phase 6: Introducing Model Class

Create Employee model.

```java
public class Employee {

    private int id;
    private String name;
    private double salary;

    // getters setters
}
```

---

## Why Model Objects?

Instead of passing every field individually:

```java
saveEmployee(
 id,
 name,
 salary,
 email,
 department
)
```

use:

```java
saveEmployee(Employee employee)
```

---

# Phase 7: @ModelAttribute

Controller:

```java
@PostMapping
@ResponseBody
public String saveEmployee(
        @ModelAttribute Employee employee) {

    return employee.toString();
}
```

Request:

```http
POST /employee

id=101
name=John
salary=50000
```

Spring automatically populates:

```java
Employee employee
```

using setter methods.

---

## Internal Working

Spring:

```text
Request Parameters
        ↓
Creates Employee Object
        ↓
Calls Setters
        ↓
Passes Object To Controller
```

---

## Important Observation

Spring uses:

```java
setId()
setName()
setSalary()
```

for binding.

Hence proper setters are mandatory.

---

# Phase 8: Employee Registration Form

Create HTML form.

```html
<form action="/employee" method="post">

    Id:
    <input type="text" name="id">

    Name:
    <input type="text" name="name">

    Salary:
    <input type="text" name="salary">

    <button type="submit">
        Save
    </button>

</form>
```

---

## Goal

Submit form data directly into:

```java
Employee employee
```

using:

```java
@ModelAttribute
```

---

# Phase 9: Validation Dependency

Add:

```xml
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
</dependency>
```

---

# Phase 10: Validation Annotations

Employee:

```java
public class Employee {

    @NotNull
    private Integer id;

    @NotBlank
    private String name;

    @Min(1000)
    private double salary;
}
```

---

## Concepts Introduced

### @NotNull

Value cannot be null.

### @NotBlank

String cannot be empty.

### @Min

Minimum allowed value.

---

# Phase 11: @Valid

Controller:

```java
@PostMapping
public String saveEmployee(
        @Valid
        @ModelAttribute Employee employee,
        BindingResult result) {

    if(result.hasErrors()) {
        return "employee-form";
    }

    return "success";
}
```

---

## Why @Valid?

Triggers validation process.

Without @Valid:

Validation annotations are ignored.

---

# Phase 12: BindingResult

Contains validation errors.

```java
result.hasErrors()
```

returns:

```text
true
```

when validation fails.

---

## Important Rule

BindingResult must immediately follow the validated object.

Correct:

```java
@Valid Employee employee,
BindingResult result
```

Incorrect:

```java
@Valid Employee employee,
Model model,
BindingResult result
```

---

# Phase 13: Introducing JSP Views

Remove:

```java
@ResponseBody
```

Return view names.

```java
@GetMapping("/form")
public String showForm() {
    return "employee-form";
}
```

Spring resolves:

```text
employee-form
        ↓
employee-form.jsp
```

---

# Phase 14: JSTL

Used inside JSPs for:

* Conditions
* Loops
* Displaying errors

Example:

```jsp
<c:if test="${not empty error}">
    Error Found
</c:if>
```

---

# Final Learning Outcomes

Students should be comfortable with:

* @Controller
* @RequestMapping
* @GetMapping
* @PostMapping
* @ResponseBody
* @RequestParam
* @PathVariable
* @ModelAttribute
* Form Submission
* Model Object Binding
* Validation
* @Valid
* BindingResult
* JSP
* JSTL

---

# Curious Questions

1. Why is @ResponseBody required?
2. How does Spring create Employee objects automatically?
3. Why are setters required for @ModelAttribute?
4. What happens if BindingResult is omitted?
5. Why must BindingResult be placed immediately after @Valid?
6. When should we use RequestParam vs PathVariable?
7. Can a form directly populate a Java object?
8. Does validation belong to Spring or Java Bean Validation?
