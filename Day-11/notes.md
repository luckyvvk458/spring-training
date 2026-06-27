# notes.md

# REST APIs using Spring MVC

## Session Objectives

By the end of today's class, you should be able to:

* Understand what REST is.
* Explain why REST APIs are required.
* Understand HTTP methods.
* Create REST Controllers.
* Build CRUD APIs using Spring MVC.
* Send and receive JSON.
* Return responses using `ResponseEntity`.
* Test REST APIs using Postman.

---

# REST API Fundamentals

## What is an API?

API stands for **Application Programming Interface**.

An API allows two applications to communicate with each other.

Example:

```
Mobile App
      |
      |
REST API
      |
      |
Database
```

The client sends a request to the server, and the server processes the request and returns a response.

---

## Why REST APIs?

Earlier web applications mainly returned HTML pages.

```
Browser
   |
Server
   |
HTML Page
```

Today, applications are developed for multiple platforms such as:

* Browser
* Mobile Application
* Desktop Application
* Smart TV
* IoT Devices

These applications require **data**, not HTML pages.

REST APIs solve this problem by returning data in JSON format.

---

## What is REST?

REST stands for **Representational State Transfer**.

REST is an architectural style used for designing web services.

Everything in REST is treated as a **Resource**.

Examples:

```
Employee
Book
Student
Order
Product
```

Each resource is identified using a URL.

```
/books

/books/10

/students
```

---

## REST Principles

### Client Server Architecture

The client and server are independent.

```
Client
   |
HTTP Request
   |
Server
```

---

### Stateless

The server does not remember previous requests.

Every request contains all required information.

Example

```
GET /books/1

GET /books/2
```

Both requests are processed independently.

---

### Resource Based URLs

Good Examples

```
/books

/books/1

/books/10
```

Avoid

```
/getBook

/createBook

/deleteBook
```

The HTTP method itself defines the operation.

---

# HTTP Request

An HTTP Request contains:

* URL
* HTTP Method
* Headers
* Request Body (Optional)

Example

```
POST /books
```

Body

```json
{
    "id":1,
    "title":"Spring",
    "author":"John",
    "price":450
}
```

---

# HTTP Response

An HTTP Response contains:

* Status Code
* Headers
* Response Body

Example

```json
{
    "id":1,
    "title":"Spring",
    "author":"John",
    "price":450
}
```

Status Code

```
200 OK
```

---

# HTTP Methods

| Method | Purpose     |
| ------ | ----------- |
| GET    | Read Data   |
| POST   | Create Data |
| PUT    | Update Data |
| DELETE | Delete Data |

Remember

```
CRUD

Create  -> POST

Read    -> GET

Update  -> PUT

Delete  -> DELETE
```

---

# Common HTTP Status Codes

| Code | Meaning               |
| ---- | --------------------- |
| 200  | Success               |
| 201  | Created               |
| 204  | No Content            |
| 400  | Bad Request           |
| 404  | Not Found             |
| 500  | Internal Server Error |

---

# JSON

JSON stands for **JavaScript Object Notation**.

REST APIs usually exchange data using JSON.

Example

```json
{
    "id":1,
    "title":"Spring Framework",
    "author":"John",
    "price":450
}
```

Advantages

* Lightweight
* Human Readable
* Easy to Parse
* Language Independent

---

# Spring MVC vs REST API

Traditional Spring MVC

```
@Controller

↓

Returns JSP
```

REST API

```
@RestController

↓

Returns JSON
```

---

# Building REST APIs

Today's application does not use a database.

Instead, data is stored in memory using an `ArrayList<Book>`.

This helps us focus only on REST concepts.

---

# Project Structure

```
com.training

    config

    controller

    model

    service
```

---

# Book Model

Create a Book class with the following fields.

```
id

title

author

price
```

Generate

* Constructors
* Getters
* Setters
* toString()

---

# Service Layer

Create

```
BookService
```

Maintain

```java
private List<Book> books = new ArrayList<>();
```

Implement

```java
findAll()

findById()

save()

update()

delete()
```

---

# REST Controller

Create

```java
@RestController
@RequestMapping("/api/books")
public class BookController {
}
```

---

# GET All Books

```java
@GetMapping
public List<Book> getBooks()
```

Request

```
GET

/api/books
```

---

# GET Book By Id

```java
@GetMapping("/{id}")
```

Request

```
GET

/api/books/1
```

Use

```java
@PathVariable
```

to capture the id from the URL.

---

# POST Book

```java
@PostMapping
```

Use

```java
@RequestBody
```

Request Body

```json
{
    "id":1,
    "title":"Spring",
    "author":"John",
    "price":450
}
```

---

# PUT Book

```java
@PutMapping("/{id}")
```

Update an existing book.

---

# DELETE Book

```java
@DeleteMapping("/{id}")
```

Delete a book using its id.

---

# RequestBody

`@RequestBody` converts incoming JSON into a Java Object.

```
JSON

↓

Book Object
```

---

# PathVariable

`@PathVariable` extracts values from the URL.

Example

```
GET

/books/5
```

becomes

```java
@PathVariable int id
```

---

# Request Flow

```
Client

↓

HTTP Request

↓

DispatcherServlet

↓

BookController

↓

BookService

↓

ArrayList<Book>

↓

BookService

↓

BookController

↓

JSON

↓

Client
```

---

# ResponseEntity

Returning only an object always sends **200 OK**.

Sometimes we need different status codes.

Examples

```
Book Created

201 Created
```

```
Book Not Found

404 Not Found
```

```
Book Deleted

204 No Content
```

Spring provides `ResponseEntity` to customize the response.

Example

```java
return ResponseEntity.ok(book);
```

Other examples

```java
ResponseEntity.ok()

ResponseEntity.status(HttpStatus.CREATED)

ResponseEntity.notFound().build()

ResponseEntity.noContent().build()
```

---

# API Testing using Postman

Postman is an API testing tool.

It allows us to send HTTP requests without creating a frontend application.

---

## Test GET

```
GET

http://localhost:8080/api/books
```

---

## Test POST

Headers

```
Content-Type

application/json
```

Body

```json
{
    "id":1,
    "title":"Spring",
    "author":"John",
    "price":450
}
```

---

## Test PUT

```
PUT

/api/books/1
```

Update the JSON body and send the request.

---

## Test DELETE

```
DELETE

/api/books/1
```

---

# Things to Observe in Postman

* URL
* HTTP Method
* Headers
* Request Body
* Response Body
* Status Code

---

# Common Mistakes

* Using `@Controller` instead of `@RestController`
* Forgetting `@RequestBody`
* Missing `@PathVariable`
* Sending invalid JSON
* Using the wrong HTTP Method
* Forgetting to set `Content-Type: application/json`

---

# Summary

Today you learned:

* What REST is
* Why REST APIs are used
* REST Principles
* HTTP Request and Response
* HTTP Methods
* JSON
* REST Controllers
* `@RestController`
* `@RequestMapping`
* `@GetMapping`
* `@PostMapping`
* `@PutMapping`
* `@DeleteMapping`
* `@RequestBody`
* `@PathVariable`
* `ResponseEntity`
* API Testing using Postman

---

# Practice Exercise

Create a **Student Management REST API**.

Student Fields

```
id

name

course

fee
```

Implement the following APIs.

```
GET

/students
```

```
GET

/students/{id}
```

```
POST

/students
```

```
PUT

/students/{id}
```

```
DELETE

/students/{id}
```

Store the data using an `ArrayList<Student>`.

In the next class, we will replace the in-memory list with a database using Spring Data JPA while keeping the REST API structure almost unchanged.
