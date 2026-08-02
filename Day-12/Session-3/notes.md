# Phase 2 -- Spring Boot

# Session 3 -- Creating REST APIs in Spring Boot

> **Goal:** Learn how to build REST APIs using Spring Boot and
> understand how HTTP requests travel through a Spring Boot application.

------------------------------------------------------------------------

# Learning Objectives

By the end of this session you will be able to:

-   Understand what a REST API is.
-   Explain REST principles.
-   Create REST endpoints using Spring Boot.
-   Use `@RestController`, `@RequestMapping`, `@GetMapping`,
    `@PostMapping`, `@PutMapping`, and `@DeleteMapping`.
-   Accept Path Variables and Request Parameters.
-   Accept JSON request bodies using `@RequestBody`.
-   Return Java objects as JSON.
-   Test APIs using Postman.

------------------------------------------------------------------------

# 1. What is an API?

API stands for **Application Programming Interface**.

An API allows two applications to communicate with each other.

Example:

``` text
Mobile App
      │
      ▼
 REST API
      │
      ▼
Database
```

Whenever you order food, book a ticket, or check your bank balance, your
application is communicating with a REST API.

------------------------------------------------------------------------

# 2. What is REST?

REST stands for **Representational State Transfer**.

It is an architectural style for building web services.

REST uses standard HTTP methods.

HTTP Method   Purpose
  ------------- -------------
GET           Read Data
POST          Create Data
PUT           Update Data
DELETE        Delete Data

Example:

``` text
GET    /students
POST   /students
PUT    /students/101
DELETE /students/101
```

------------------------------------------------------------------------

# 3. Request Flow in Spring Boot

``` text
Client
   │
HTTP Request
   │
Embedded Tomcat
   │
DispatcherServlet
   │
@RestController
   │
@Service
   │
@Repository
   │
Database

Response (JSON)
```

The DispatcherServlet receives every request and forwards it to the
appropriate controller.

------------------------------------------------------------------------

# 4. Creating Your First REST API

## Project Structure

``` text
train-service
│
├── controller
│     └── TrainController.java
├── service
├── model
└── TrainServiceApplication.java
```

------------------------------------------------------------------------

# 5. @RestController

`@RestController` tells Spring Boot that the class handles REST
requests.

It combines:

-   `@Controller`
-   `@ResponseBody`

Example:

``` java
@RestController
public class TrainController {

    @GetMapping("/hello")
    public String hello() {
        return "Welcome to Spring Boot REST API";
    }

}
```

Run the application.

Visit:

    http://localhost:8080/hello

Output:

``` text
Welcome to Spring Boot REST API
```

------------------------------------------------------------------------

# 6. @RequestMapping

Used to define a common base URL.

``` java
@RestController
@RequestMapping("/trains")
public class TrainController {

}
```

Now every endpoint begins with:

    /trains

------------------------------------------------------------------------

# 7. GET API

``` java
@RestController
@RequestMapping("/trains")
public class TrainController {

    @GetMapping
    public List<String> getTrains() {
        return List.of("Rajdhani","Shatabdi","Vande Bharat");
    }

}
```

URL

    GET /trains

Response

``` json
[
  "Rajdhani",
  "Shatabdi",
  "Vande Bharat"
]
```

------------------------------------------------------------------------

# 8. Returning Objects

Model

``` java
public class Train {

    private int id;
    private String name;
    private String source;
    private String destination;

    // Constructors
    // Getters
    // Setters
}
```

Controller

``` java
@GetMapping("/{id}")
public Train getTrain() {

    return new Train(
            1,
            "Vande Bharat",
            "Hyderabad",
            "Bengaluru");
}
```

Spring Boot automatically converts the Java object into JSON using
Jackson.

Response

``` json
{
  "id":1,
  "name":"Vande Bharat",
  "source":"Hyderabad",
  "destination":"Bengaluru"
}
```

------------------------------------------------------------------------

# 9. Path Variables

Used when a value is part of the URL.

``` java
@GetMapping("/{id}")
public String findTrain(
        @PathVariable int id) {

    return "Train Id : " + id;
}
```

Example

    GET /trains/101

Output

    Train Id : 101

------------------------------------------------------------------------

# 10. Request Parameters

Used for optional values.

``` java
@GetMapping("/search")
public String search(
        @RequestParam String source,
        @RequestParam String destination) {

    return source + " -> " + destination;
}
```

URL

    GET /trains/search?source=Hyderabad&destination=Chennai

------------------------------------------------------------------------

# 11. POST API

Accept JSON using `@RequestBody`.

``` java
@PostMapping
public Train createTrain(
        @RequestBody Train train) {

    return train;
}
```

Request

``` json
{
  "id":101,
  "name":"Garib Rath",
  "source":"Delhi",
  "destination":"Mumbai"
}
```

Response

``` json
{
  "id":101,
  "name":"Garib Rath",
  "source":"Delhi",
  "destination":"Mumbai"
}
```

------------------------------------------------------------------------

# 12. PUT API

``` java
@PutMapping("/{id}")
public String updateTrain(
        @PathVariable int id,
        @RequestBody Train train){

    return "Updated Train : " + id;
}
```

------------------------------------------------------------------------

# 13. DELETE API

``` java
@DeleteMapping("/{id}")
public String deleteTrain(
        @PathVariable int id){

    return "Deleted Train : " + id;
}
```

------------------------------------------------------------------------

# 14. Testing with Postman

1.  Start the application.
2.  Open Postman.
3.  Create a GET request.
4.  Enter:

```{=html}
<!-- -->
```
    http://localhost:8080/trains

5.  Click **Send**.

Repeat for POST, PUT and DELETE.

------------------------------------------------------------------------

# 15. REST API Best Practices

-   Use nouns instead of verbs.
-   Use plural resource names.

Good:

    /students
    /trains
    /employees

Avoid:

    /getStudents
    /createTrain
    /deleteEmployee

Return appropriate HTTP status codes.

Status   Meaning
  -------- --------------
200      Success
201      Created
400      Bad Request
404      Not Found
500      Server Error

------------------------------------------------------------------------

# 16. Hands-on Exercise

Create a **Student Management REST API**.

Model:

``` text
Student
- id
- name
- course
- marks
```

Implement:

-   GET /students
-   GET /students/{id}
-   POST /students
-   PUT /students/{id}
-   DELETE /students/{id}

Use an in-memory `List<Student>` instead of a database.

------------------------------------------------------------------------

# Interview Questions

1.  What is REST?
2.  Difference between @Controller and @RestController?
3.  What is @RequestMapping?
4.  Difference between @PathVariable and @RequestParam?
5.  What is @RequestBody?
6.  How does Spring Boot convert Java objects into JSON?
7.  Which library performs JSON serialization?
8.  What HTTP methods are commonly used in REST APIs?

------------------------------------------------------------------------

# Key Takeaways

-   REST APIs enable communication between applications.
-   Spring Boot makes building REST APIs simple.
-   `@RestController` returns JSON responses.
-   `@RequestMapping` defines a common URL.
-   `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping`
    map HTTP methods.
-   `@PathVariable` reads values from URLs.
-   `@RequestParam` reads query parameters.
-   `@RequestBody` converts JSON into Java objects.
-   Jackson automatically converts Java objects into JSON.
