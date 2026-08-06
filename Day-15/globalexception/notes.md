# Global Exception Handling in Spring Boot

## Student Notes (Train Service Project)

> **Prerequisites** - Spring Boot - Spring Data JPA - REST APIs - Train
> Service CRUD application

------------------------------------------------------------------------

# Learning Objectives

By the end of this session you will understand:

-   Why exception handling is required
-   Problems with controller-level try/catch
-   Local vs Global exception handling
-   `@ControllerAdvice`
-   `@ExceptionHandler`
-   Custom Exceptions
-   Standard Error Response
-   Integrating Global Exception Handling into the Train Service project

------------------------------------------------------------------------

# 1. Why do we need Exception Handling?

Imagine our Train Service exposes:

    GET /trains/1

If Train 1 exists:

    200 OK

If Train 100 doesn't exist and our service throws:

``` java
throw new RuntimeException("Train not found");
```

Without handling, Spring returns a generic 500 response.

That is not user friendly.

Clients expect meaningful responses.

Example:

``` json
{
  "timestamp":"2026-08-06T09:30:00",
  "status":404,
  "error":"Not Found",
  "message":"Train not found with id 100"
}
```

------------------------------------------------------------------------

# 2. The Wrong Way

Many beginners write:

``` java
@GetMapping("/{id}")
public Train getTrain(@PathVariable Integer id){
    try{
        return trainService.getTrain(id);
    }catch(Exception e){
        throw e;
    }
}
```

Every controller repeats the same code.

Problems:

-   Duplicate code
-   Hard to maintain
-   Different error formats

------------------------------------------------------------------------

# 3. Better Idea

Move exception handling to one central place.

    Controller
         |
    Service throws Exception
         |
    Global Exception Handler
         |
    Standard JSON Response

This is called **Global Exception Handling**.

------------------------------------------------------------------------

# 4. Key Annotations

## `@ControllerAdvice`

Marks a class that handles exceptions from all controllers.

## `@ExceptionHandler`

Marks a method that handles a particular exception.

------------------------------------------------------------------------

# 5. Create a Custom Exception

Package:

    exception

``` java
package com.training.train_service.exception;

public class TrainNotFoundException extends RuntimeException {

    public TrainNotFoundException(String message){
        super(message);
    }
}
```

Why custom exception?

Instead of:

``` java
throw new RuntimeException("Train not found");
```

Use:

``` java
throw new TrainNotFoundException("Train not found with id " + id);
```

Much clearer.

------------------------------------------------------------------------

# 6. Throw Exception from Service

``` java
public Train getTrainById(Integer id){

    return trainRepository.findById(id)
            .orElseThrow(() ->
                new TrainNotFoundException(
                    "Train not found with id " + id));
}
```

Notice:

Controller stays clean.

Service contains business logic.

------------------------------------------------------------------------

# 7. Error Response Model

Create:

``` java
package com.training.train_service.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // constructors
    // getters
    // setters
}
```

Why?

Every API should return the same structure.

Example:

``` json
{
  "timestamp":"2026-08-06T09:45:00",
  "status":404,
  "error":"Not Found",
  "message":"Train not found with id 5",
  "path":"/trains/5"
}
```

------------------------------------------------------------------------

# 8. Global Exception Handler

``` java
package com.training.train_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

# 9. Flow

    GET /trains/100
          |
    Controller
          |
    Service
          |
    TrainNotFoundException
          |
    @ControllerAdvice
          |
    404 Response

------------------------------------------------------------------------

# 10. Handle Validation Errors

If using `@Valid`:

``` java
@PostMapping
public Train save(@Valid @RequestBody Train train){
    return trainService.save(train);
}
```

Handle:

``` java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(...){
    ...
}
```

Return:

``` json
{
  "status":400,
  "message":"Validation Failed"
}
```

------------------------------------------------------------------------

# 11. Handle Generic Exceptions

Always keep a fallback.

``` java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(...){
    ...
}
```

Return HTTP 500.

------------------------------------------------------------------------

# 12. Project Structure

    train-service
    |
    +-- controller
    +-- service
    +-- repository
    +-- model
    +-- exception
        |
        +-- ErrorResponse
        +-- TrainNotFoundException
        +-- GlobalExceptionHandler

------------------------------------------------------------------------

# 13. End-to-End Example

Request

    GET /trains/999

Service

    Repository returns Empty

↓

    Throw TrainNotFoundException

↓

    GlobalExceptionHandler catches it

↓

Response

``` json
{
  "timestamp":"2026-08-06T10:00:00",
  "status":404,
  "error":"Not Found",
  "message":"Train not found with id 999",
  "path":"/trains/999"
}
```

------------------------------------------------------------------------

# Best Practices

-   Create custom exceptions for business scenarios.
-   Never return stack traces to clients.
-   Use proper HTTP status codes.
-   Keep controllers free from try/catch.
-   Return a consistent error response.
-   Add a generic exception handler as a safety net.

------------------------------------------------------------------------

# Classroom Exercise

1.  Create `TrainNotFoundException`.
2.  Throw it from `getTrainById()`.
3.  Create `ErrorResponse`.
4.  Implement `GlobalExceptionHandler`.
5.  Verify:
    -   Existing train → 200
    -   Missing train → 404
    -   Invalid request → 400
    -   Unexpected error → 500

------------------------------------------------------------------------

# Summary

-   Exceptions are inevitable.
-   Controllers should not contain repetitive try/catch.
-   `@ControllerAdvice` centralizes exception handling.
-   `@ExceptionHandler` maps exceptions to HTTP responses.
-   Custom exceptions make code expressive.
-   Standard error responses make APIs consistent and production-ready.
