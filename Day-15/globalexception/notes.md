# Global Exception Handling -- Detailed Student Notes

## Train Service Project (Step-by-Step)

> **Prerequisites**
>
> -   Spring Boot
> -   REST APIs
> -   Spring Data JPA
> -   Existing Train Service CRUD Application

------------------------------------------------------------------------

# Session Goal

By the end of this session you will understand:

-   What is an Exception?
-   Why do we need Exception Handling?
-   Why `try-catch` inside every controller is a bad idea?
-   What is Global Exception Handling?
-   What is `@ControllerAdvice`?
-   What is `@ExceptionHandler`?
-   How do we integrate it into our existing Train Service application?

------------------------------------------------------------------------

# Step 1: Start with a Problem

Suppose our database contains:

ID   Train Name
  ---- -----------------
1    Mumbai Express
2    Chennai Express

Request:

``` http
GET /trains/1
```

Response:

``` json
{
  "id":1,
  "trainName":"Mumbai Express"
}
```

Everything works.

Now call:

``` http
GET /trains/100
```

Question:

> What should happen if Train 100 does not exist?

------------------------------------------------------------------------

# Step 2: Current Service Implementation

``` java
public Train getTrainById(Integer id) {
    return trainRepository.findById(id).get();
}
```

Flow:

    Repository

    ↓

    findById(100)

    ↓

    Optional.empty()

    ↓

    .get()

    ↓

    NoSuchElementException

Java throws an exception because there is no value inside the Optional.

------------------------------------------------------------------------

# Step 3: What does the client receive?

Without handling the exception, Spring Boot returns a generic response.

``` json
{
  "status":500,
  "error":"Internal Server Error"
}
```

Problems:

-   Client doesn't know what went wrong.
-   500 is misleading.
-   The train simply wasn't found.

A better response would be:

``` json
{
  "status":404,
  "message":"Train not found with id 100"
}
```

------------------------------------------------------------------------

# Step 4: Beginner Solution

Many beginners write:

``` java
@GetMapping("/{id}")
public Train getTrain(@PathVariable Integer id){

    try{
        return trainService.getTrainById(id);
    }catch(Exception e){
        throw e;
    }
}
```

Looks fine for one API.

But imagine:

-   TrainController → 20 APIs
-   BookingController → 15 APIs
-   UserController → 25 APIs

Every method contains repeated try-catch blocks.

Problems:

-   Duplicate code
-   Difficult maintenance
-   Different response formats

------------------------------------------------------------------------

# Step 5: Better Design

Instead of handling exceptions inside every controller, create one
central place.

    Controller
         │
    Service throws Exception
         │
    Global Exception Handler
         │
    Standard JSON Response

This is called **Global Exception Handling**.

------------------------------------------------------------------------

# Step 6: Create a Custom Exception

Instead of:

``` java
throw new RuntimeException("Train not found");
```

Create:

``` java
package com.training.train_service.exception;

public class TrainNotFoundException extends RuntimeException {

    public TrainNotFoundException(String message){
        super(message);
    }
}
```

Benefits:

-   Exception name clearly explains the problem.
-   Easier debugging.
-   Cleaner business logic.

------------------------------------------------------------------------

# Step 7: Throw Custom Exception

Replace

``` java
return trainRepository.findById(id).get();
```

with

``` java
return trainRepository.findById(id)
        .orElseThrow(() ->
            new TrainNotFoundException(
                "Train not found with id " + id));
```

Flow:

    Repository

    ↓

    Train Found?

    ↓

    No

    ↓

    Throw TrainNotFoundException

Notice:

The service does **not** catch the exception.

------------------------------------------------------------------------

# Step 8: Exception Propagation

Question:

Who catches this exception?

Answer:

Nobody in the Service or Controller.

It propagates upward.

    Repository

    ↓

    Service

    ↓

    Controller

    ↓

    Spring Framework

    ↓

    Global Exception Handler

------------------------------------------------------------------------

# Step 9: Introduce @ControllerAdvice

Create:

``` java
@ControllerAdvice
public class GlobalExceptionHandler {

}
```

Meaning:

> This class handles exceptions thrown by all controllers.

Think of it as:

-   `@RestController` → Handles Requests
-   `@ControllerAdvice` → Handles Exceptions

------------------------------------------------------------------------

# Step 10: Handle TrainNotFoundException

``` java
@ExceptionHandler(TrainNotFoundException.class)
public ResponseEntity<String> handleTrainNotFound(
        TrainNotFoundException ex){

    return new ResponseEntity<>(
            ex.getMessage(),
            HttpStatus.NOT_FOUND);
}
```

Read this in English:

"If TrainNotFoundException occurs anywhere, call this method."

Spring calls it automatically.

------------------------------------------------------------------------

# Step 11: Improve the Response

Returning plain text isn't ideal.

Instead return a standard object.

Example:

``` json
{
  "timestamp":"2026-08-06T10:00:00",
  "status":404,
  "error":"Not Found",
  "message":"Train not found with id 100",
  "path":"/trains/100"
}
```

Professional APIs follow a consistent error format.

------------------------------------------------------------------------

# Step 12: ErrorResponse Class

``` java
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
```

Think of it like:

-   `Train` stores Train information.
-   `ErrorResponse` stores Error information.

------------------------------------------------------------------------

# Step 13: Complete GlobalExceptionHandler

``` java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TrainNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrainNotFound(
            TrainNotFoundException ex,
            HttpServletRequest request){

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return new ResponseEntity<>(response,
                HttpStatus.NOT_FOUND);
    }
}
```

------------------------------------------------------------------------

# Step 14: Complete Flow

    GET /trains/100

    ↓

    Controller

    ↓

    Service

    ↓

    Repository

    ↓

    Train Missing

    ↓

    Throw TrainNotFoundException

    ↓

    @ControllerAdvice

    ↓

    @ExceptionHandler

    ↓

    ErrorResponse

    ↓

    404 JSON Response

    ↓

    Client

------------------------------------------------------------------------

# Best Practices

-   Create meaningful custom exceptions.
-   Keep controllers free from try-catch blocks.
-   Handle exceptions in one place.
-   Return proper HTTP status codes.
-   Always return a consistent error response.

------------------------------------------------------------------------

# Classroom Exercise

1.  Create `TrainNotFoundException`
2.  Throw it from the Service layer.
3.  Create `ErrorResponse`.
4.  Create `GlobalExceptionHandler`.
5.  Test:
    -   Existing Train → 200
    -   Missing Train → 404
    -   Invalid Request → 400 (later with Validation)
    -   Unexpected Error → 500

------------------------------------------------------------------------

# Summary

-   Exceptions are part of every application.
-   Avoid repetitive try-catch in controllers.
-   Use custom exceptions to represent business errors.
-   `@ControllerAdvice` centralizes exception handling.
-   `@ExceptionHandler` maps exceptions to appropriate HTTP responses.
-   `ErrorResponse` gives clients a consistent API response format.
