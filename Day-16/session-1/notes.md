# Session 3: Global Exception Handling (Spring Boot)

## Learning Objective

By the end of this session, students should understand:

-   Why exception handling is needed
-   Default Spring Boot exception behavior
-   Why `try-catch` is not a good long-term solution
-   Local Exception Handling using `@ExceptionHandler`
-   Global Exception Handling using `@ControllerAdvice`
-   Why Custom Exceptions are preferred over `RuntimeException`
-   Returning meaningful HTTP status codes

------------------------------------------------------------------------

# Evolution 1 -- No Exception Handling

## Service

``` java
public Train findTrainById(int id) {

    return trainRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Train Not Found"));

}
```

## Controller

``` java
@GetMapping("/{id}")
public Train findTrainById(@PathVariable int id) {
    return trainsService.findTrainById(id);
}
```

## Flow

    Request
       ↓
    Controller
       ↓
    Service
       ↓
    Repository
       ↓
    RuntimeException
       ↓
    Spring Boot
       ↓
    500 Internal Server Error

### Problem

Although we know the train is not found, the client receives **500
Internal Server Error**.

Expected response:

    404 Not Found
    Train Not Found

------------------------------------------------------------------------

# Evolution 2 -- try-catch in Controller

``` java
@GetMapping("/{id}")
public ResponseEntity<?> findTrainById(@PathVariable int id) {

    try {

        Train train = trainsService.findTrainById(id);
        return ResponseEntity.ok(train);

    } catch (RuntimeException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());

    }

}
```

### Output

    404 Not Found

    Train Not Found

### Drawback

Every API now needs:

    try {

    }
    catch(){

    }

This leads to duplicated code.

------------------------------------------------------------------------

# Evolution 3 -- Local Exception Handling

Spring allows us to move exception handling into a separate method
inside the same controller.

``` java
@RestController
@RequestMapping("/trains")
public class TrainController {

    @GetMapping("/{id}")
    public Train findTrainById(@PathVariable int id) {

        return trainsService.findTrainById(id);

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());

    }
}
```

### Flow

    Controller Method
            ↓
    RuntimeException
            ↓
    Spring finds @ExceptionHandler
            ↓
    handleRuntimeException()
            ↓
    404 + Train Not Found

### Benefit

-   No try-catch in controller methods.
-   Cleaner controller.

### Limitation

This handler works **only for this controller**.

------------------------------------------------------------------------

# Evolution 4 -- Global Exception Handling

If we have:

    TrainController
    TicketController
    UserController

Writing the same `@ExceptionHandler` in every controller again causes
duplication.

## Solution

Create a separate class.

    exception
        GlobalExceptionHandler.java

``` java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());

    }

}
```

Now remove the `@ExceptionHandler` method from every controller.

### Flow

    User
       ↓
    Controller
       ↓
    Service
       ↓
    Repository
       ↓
    RuntimeException
       ↓
    GlobalExceptionHandler
       ↓
    404 Not Found

### Benefit

One place handles exceptions for all controllers.

------------------------------------------------------------------------

# Evolution 5 -- Why Custom Exceptions?

Current code:

``` java
throw new RuntimeException("Train Not Found");
```

This works, but tomorrow we may also have:

``` java
throw new RuntimeException("Seat Not Available");
```

``` java
throw new RuntimeException("Payment Failed");
```

Everything becomes a `RuntimeException`.

Spring cannot identify business errors by exception type.

------------------------------------------------------------------------

# Create a Custom Exception

``` java
package com.training.demo_train_service.exception;

public class TrainNotFoundException extends RuntimeException {

    public TrainNotFoundException(String message) {
        super(message);
    }
}
```

------------------------------------------------------------------------

# Update the Service

``` java
public Train findTrainById(int id) {

    return trainRepository.findById(id)
            .orElseThrow(() ->
                new TrainNotFoundException("Train Not Found"));

}
```

------------------------------------------------------------------------

# Update the Global Exception Handler

``` java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TrainNotFoundException.class)
    public ResponseEntity<String> handleTrainNotFoundException(
            TrainNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());

    }

}
```

### Why is this better?

Instead of a generic exception:

    RuntimeException

we now have:

    TrainNotFoundException

The exception itself explains the business problem.

------------------------------------------------------------------------

# Future Growth

Tomorrow we can add:

    SeatNotAvailableException
    PaymentFailedException
    UserNotFoundException
    TicketAlreadyCancelledException

Each exception can return a different HTTP status code.

------------------------------------------------------------------------

# Final Evolution

    No Exception Handling
            ↓
    500 Internal Server Error
            ↓
    try-catch in Controller
            ↓
    Repeated Code
            ↓
    @ExceptionHandler (Local)
            ↓
    Repeated in every Controller
            ↓
    @ControllerAdvice (Global)
            ↓
    Custom Exception
            ↓
    Meaningful Business Exception
            ↓
    Professional REST API

------------------------------------------------------------------------

# Key Takeaways

-   Throw exceptions for exceptional situations.
-   Avoid writing `try-catch` in every controller method.
-   Use `@ExceptionHandler` for local handling.
-   Use `@ControllerAdvice` for application-wide handling.
-   Prefer custom exceptions over generic `RuntimeException`.
-   Return meaningful HTTP status codes and messages.
