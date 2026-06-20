# Day 5 - Complete Notes

# HTTP Basics, Servlets, Spring MVC & DispatcherServlet

Duration: 8 Hours

* Session 1 : HTTP Basics & Request-Response Cycle (2 Hours)
* Session 2 : Servlet Fundamentals (2 Hours)
* Session 3 : Spring MVC Architecture (2 Hours)
* Session 4 : DispatcherServlet & Controllers (2 Hours)

---

# Session 1

# HTTP Basics & Request Response Cycle

---

# Introduction

Yesterday we learned:

```text
Java Application
     ↓
Service
     ↓
DAO
     ↓
JdbcTemplate
     ↓
Database
```

Today's Question:

```text
How does Browser communicate with Java Application?
```

Answer:

```text
HTTP Protocol
```

---

# What is HTTP?

HTTP stands for:

```text
Hyper Text Transfer Protocol
```

Protocol means:

```text
Rules of Communication
```

---

# Real Life Example

```text
Customer
    ↓
Order
    ↓
Restaurant
    ↓
Food
```

Similarly

```text
Browser
    ↓
HTTP Request
    ↓
Server
    ↓
HTTP Response
    ↓
Browser
```

---

# Request Response Cycle

```text
Browser
    ↓
HTTP Request
    ↓
Server
    ↓
Database
    ↓
HTTP Response
    ↓
Browser
```

---

# HTTP Request Structure

```text
Method
URL
Headers
Body
```

Example:

```http
POST /students

Content-Type: application/json

{
   "id":101,
   "name":"Rahul"
}
```

---

# HTTP Methods

## GET

Used to fetch data.

```http
GET /students
```

---

## POST

Used to create data.

```http
POST /students
```

---

## PUT

Used to update data.

```http
PUT /students/101
```

---

## DELETE

Used to delete data.

```http
DELETE /students/101
```

---

# CRUD Mapping

```text
Create -> POST

Read -> GET

Update -> PUT

Delete -> DELETE
```

---

# Understanding Headers

Question:

```text
Where are headers used?
```

Headers provide metadata.

---

Example:

```http
Authorization: Bearer xyz
```

Purpose:

```text
Authentication
```

---

Example:

```http
Content-Type: application/json
```

Purpose:

```text
Body contains JSON
```

---

Example:

```http
Accept: application/json
```

Purpose:

```text
Response should be JSON
```

---

# Real Life Analogy

```text
Courier Package
```

Package:

```text
Body
```

Address:

```text
URL
```

Instructions:

```text
Handle Carefully
Fragile
Urgent
```

These instructions are:

```text
Headers
```

---

# HTTP Status Codes

```text
200 -> Success

201 -> Created

404 -> Not Found

500 -> Internal Server Error
```

---

# Stateless Protocol

Definition:

```text
HTTP does not remember previous requests.

Every request is independent.
```

---

# Session 1 Assignment

1. Explain HTTP Request Structure.
2. Difference between GET and POST.
3. Explain 200, 404 and 500.
4. What is a Stateless Protocol?
5. Give 5 examples of HTTP headers.

=====================================================

# Session 2

# Servlet Fundamentals

---

# Problem Statement

Browser sends:

```text
GET /students
```

Question:

```text
Can Browser directly call DAO?
```

No.

Can Browser directly call Service?

No.

Need a component capable of handling HTTP.

Answer:

```text
Servlet
```

---

# What is Servlet?

Definition:

```text
Servlet is a Java class
that receives HTTP requests
and sends HTTP responses.
```

---

# JDBC vs Servlet

```text
JDBC
   ↓
Java ↔ Database
```

```text
Servlet
   ↓
Java ↔ Browser
```

---

# What is Tomcat?

Tomcat is:

```text
Servlet Container
```

Responsibilities:

```text
Create Servlet Objects

Manage Lifecycle

Handle Requests

Destroy Servlet
```

---

# Servlet Architecture

```text
Browser
   ↓
Tomcat
   ↓
Servlet
   ↓
Response
   ↓
Browser
```

---

# Servlet Example

```java
@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    @Override
    public void init() {
        System.out.println("StudentServlet Loaded");
    }

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");

        response.getWriter()
                .println("<h2>Id : 101</h2>");

        response.getWriter()
                .println("<h2>Name : Rahul</h2>");

        System.out.println("doGet called");
    }

    @Override
    public void destroy() {
        System.out.println("Servlet Destroyed");
    }
}
```

URL:

```text
http://localhost:8080/servletexample/student
```

---

# Servlet Lifecycle

```text
Constructor
      ↓
init()
      ↓
service()
      ↓
doGet()
doPost()
doPut()
doDelete()
      ↓
destroy()
```

---

# Lifecycle Demonstration

Start Tomcat

Output:

```text
StudentServlet Loaded
```

Only once.

---

Refresh browser

Output:

```text
doGet called
```

multiple times.

---

Stop Tomcat

Output:

```text
Servlet Destroyed
```

---

# HttpServletRequest

Represents:

```text
Incoming Request
```

Contains:

```text
Headers

Parameters

Cookies

Body
```

---

# HttpServletResponse

Represents:

```text
Outgoing Response
```

Example:

```java
response.getWriter()
```

---

# CRUD Servlet Example

```java
@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    protected void doGet(...) {
        System.out.println("Read Student");
    }

    protected void doPost(...) {
        System.out.println("Create Student");
    }

    protected void doPut(...) {
        System.out.println("Update Student");
    }

    protected void doDelete(...) {
        System.out.println("Delete Student");
    }
}
```

---

# Where Is Plumbing Code?

Requirement:

```text
Save Student
```

Servlet:

```java
String id =
        request.getParameter("id");

String name =
        request.getParameter("name");

int studentId =
        Integer.parseInt(id);

Student student =
        new Student();

student.setId(studentId);
student.setName(name);

StudentService service =
        new StudentService();

service.save(student);

response.getWriter()
        .println("Saved");
```

Business Logic:

```java
service.save(student);
```

Everything else is:

```text
Plumbing Code
```

---

# Session 2 Assignment

1. Explain Servlet Lifecycle.
2. Difference between init() and destroy().
3. What is Tomcat?
4. Explain HttpServletRequest.
5. Explain HttpServletResponse.
6. Create EmployeeServlet.
7. Implement CRUD methods in one servlet.

=====================================================

# Session 3

# Spring MVC Architecture

---

# Why Spring MVC?

Servlet applications contain:

```text
Request Parsing

Type Conversion

Object Creation

Response Writing
```

This is called:

```text
Plumbing Code
```

---

# Spring MVC Goal

```text
Reduce Plumbing Code

Focus on Business Logic
```

---

# Important Fact

Spring MVC is built on top of:

```text
Servlets
```

Spring MVC did NOT replace Servlets.

---

# Architecture

Servlet World

```text
Browser
   ↓
Tomcat
   ↓
Servlet
```

Spring MVC World

```text
Browser
   ↓
Tomcat
   ↓
DispatcherServlet
   ↓
Controller
```

---

# MVC

MVC stands for:

```text
Model
View
Controller
```

---

# Model

Contains data.

Example:

```java
Student
Employee
Product
```

---

# View

User Interface.

Examples:

```text
HTML

JSP

Thymeleaf
```

---

# Controller

Handles requests.

Example:

```java
@Controller
public class StudentController {

}
```

---

# First Controller

```java
@Controller
public class StudentController {

    @GetMapping("/student")
    public String getStudent() {

        return "student";
    }
}
```

---

# Servlet vs Controller

Servlet:

```java
@WebServlet("/student")
```

Controller:

```java
@GetMapping("/student")
```

---

# Servlet

```java
request.getParameter()
```

Spring MVC

```java
@RequestParam
```

---

# Servlet

```java
response.getWriter()
```

Spring MVC

```java
return
```

---

# Session 3 Assignment

1. Explain MVC.
2. Difference between Servlet and Controller.
3. Why was Spring MVC introduced?
4. Explain Spring MVC Architecture.
5. Create StudentController.
6. Create EmployeeController.

=====================================================

# Session 4

# DispatcherServlet & Controllers

---

# What is DispatcherServlet?

Definition:

```text
DispatcherServlet is the Front Controller
of Spring MVC.
```

---

# Airport Analogy

```text
Passenger
    ↓
Reception
    ↓
Correct Gate
```

Reception:

```text
DispatcherServlet
```

Gate:

```text
Controller
```

---

# Complete Request Flow

```text
Browser
   ↓
Tomcat
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Service
   ↓
DAO
   ↓
JdbcTemplate
   ↓
Database
```

---

# How DispatcherServlet Works

Request:

```http
GET /students
```

DispatcherServlet checks:

```text
Who handles /students?
```

Finds:

```java
@GetMapping("/students")
```

Calls controller method.

---

# CRUD Controller

```java
@RestController
@RequestMapping("/students")
public class StudentController {

    @GetMapping
    public List<Student> getAll() {
        return null;
    }

    @GetMapping("/{id}")
    public Student getById() {
        return null;
    }

    @PostMapping
    public void save() {
    }

    @PutMapping("/{id}")
    public void update() {
    }

    @DeleteMapping("/{id}")
    public void delete() {
    }
}
```

---

# Servlet vs Spring MVC

Servlet:

```java
String id =
        request.getParameter("id");

Integer.parseInt(id);

response.getWriter();
```

Spring MVC:

```java
@PostMapping("/student")
public String saveStudent(
        @RequestBody Student student) {

    service.save(student);

    return "Saved";
}
```

---

# Advantages of Spring MVC

```text
Less Boilerplate Code

Dependency Injection

Automatic Mapping

Automatic JSON Conversion

Better Architecture

Separation of Concerns
```

---

# Day 5 Final Architecture

```text
Browser
   ↓
HTTP Request
   ↓
Tomcat
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Service
   ↓
DAO
   ↓
JdbcTemplate
   ↓
Database
```

Response:

```text
Database
   ↓
JdbcTemplate
   ↓
DAO
   ↓
Service
   ↓
Controller
   ↓
DispatcherServlet
   ↓
Browser
```

=====================================================

# Day 5 Interview Questions

1. What is HTTP?
2. What are HTTP methods?
3. What are HTTP headers?
4. What is a Servlet?
5. What is Tomcat?
6. Explain Servlet Lifecycle.
7. What is HttpServletRequest?
8. What is HttpServletResponse?
9. What is Plumbing Code?
10. Why was Spring MVC introduced?
11. What is MVC?
12. What is DispatcherServlet?
13. Why is DispatcherServlet called Front Controller?
14. Difference between Servlet and Controller?
15. Explain complete Browser → Database → Browser flow.
