# Session 1: Spring AOP (`@Before` and `@Around`)

## Student Notes

Welcome! In this session, you will learn **why Spring AOP exists**,
**what problems it solves**, and **how to use it** in a Spring Boot
application.

> **Prerequisite:** You should have completed the Train Management CRUD
> application using Spring Boot, Spring Data JPA and MySQL.

------------------------------------------------------------------------

# Learning Outcomes

After completing this session, you should be able to:

-   Explain the need for Spring AOP.
-   Identify Cross-Cutting Concerns.
-   Understand Aspect, Advice, Join Point, Pointcut, Proxy and Weaving.
-   Implement `@Before` and `@Around` advice.
-   Measure API execution time using AOP.
-   Explain how Spring uses proxies to apply advice.

------------------------------------------------------------------------

# 1. Recap

You have already developed the following application.

``` text
Postman / Browser
        │
        ▼
TrainController
        │
        ▼
TrainService
        │
        ▼
TrainRepository
        │
        ▼
MySQL
```

Your application provides CRUD APIs:

-   GET /trains
-   GET /trains/{id}
-   POST /trains
-   PUT /trains/{id}
-   DELETE /trains/{id}

The application is working correctly.

------------------------------------------------------------------------

# 2. A New Requirement

Imagine your team receives the following requirement:

> "Log every API request. Record when it starts and when it completes."

Your first thought might be to add logging inside every controller
method.

Example:

``` java
@GetMapping("/{id}")
public Train getTrain(@PathVariable Long id){

    System.out.println("Entering GET API");

    Train train = trainService.getTrain(id);

    System.out.println("Leaving GET API");

    return train;
}
```

This works.

However, the same code must now be added to every controller method.

Think about the following questions:

-   What if your application has 50 APIs?
-   What if the logging message changes later?
-   Would you have to modify every controller?

This leads to duplicate code.

------------------------------------------------------------------------

# 3. Cross-Cutting Concern

Your controller should focus on business operations such as:

-   Fetch Train
-   Save Train
-   Update Train
-   Delete Train

Logging is different.

Logging is required across many modules.

This type of functionality is called a **Cross-Cutting Concern**.

Examples include:

-   Logging
-   Security
-   Transactions
-   Auditing
-   Performance Monitoring

Spring AOP is designed to handle these concerns.

------------------------------------------------------------------------

# 4. What is Spring AOP?

**AOP** stands for **Aspect Oriented Programming**.

It allows you to execute common functionality without modifying your
business logic.

Instead of writing logging code in every controller method, you write it
once inside an Aspect.

------------------------------------------------------------------------

# 5. Adding Spring AOP

Add the following dependency to your project.

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

Run the application.

Nothing changes yet because no Aspect has been created.

------------------------------------------------------------------------

# 6. Creating an Aspect

Create a new class.

``` java
@Component
@Aspect
public class LoggingAspect {

}
```

The class is now identified as an Aspect.

However, it still contains no Advice, so nothing executes.

------------------------------------------------------------------------

# 7. Using @Before Advice

``` java
@Before("execution(* com.training.controller.*.*(..))")
public void before(){

    System.out.println("Before Controller");

}
```

Whenever a controller method matches the pointcut, this advice executes
first.

------------------------------------------------------------------------

# 8. Understanding the Pointcut

``` java
execution(* com.training.controller.*.*(..))
```

Expression   Meaning
  ------------ ------------------------
execution    Match method execution
\*           Any return type
controller   Package name
\*           Any class
\*           Any method
(..)         Any arguments

------------------------------------------------------------------------

# 9. Using JoinPoint

JoinPoint provides information about the intercepted method.

Example:

``` java
@Before("execution(* com.training.controller.*.*(..))")
public void before(JoinPoint joinPoint){

    System.out.println(joinPoint.getSignature().getName());

}
```

Output:

``` text
getTrain
saveTrain
deleteTrain
```

------------------------------------------------------------------------

# 10. Printing a Timestamp

``` java
DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

String time =
        LocalDateTime.now().format(formatter);

System.out.println(time);
```

------------------------------------------------------------------------

# 11. Understanding Spring Proxy

Spring does not expose the controller directly.

Instead, it creates a Proxy.

``` text
Request
   │
   ▼
Spring Proxy
   │
   ▼
TrainController
```

The Proxy executes AOP advice before and/or after calling the real
controller.

------------------------------------------------------------------------

# 12. @Around Advice

`@Around` provides complete control over method execution.

``` java
@Around("execution(* com.training.controller.*.*(..))")
public Object around(ProceedingJoinPoint joinPoint)
        throws Throwable {

    System.out.println("Before");

    Object result = joinPoint.proceed();

    System.out.println("After");

    return result;
}
```

Without `proceed()`, the target method will never execute.

------------------------------------------------------------------------

# 13. Measuring Execution Time

``` java
long start = System.currentTimeMillis();

Object result = joinPoint.proceed();

long end = System.currentTimeMillis();

System.out.println("Execution Time : "
        + (end - start) + " ms");
```

This is one of the most common real-world uses of AOP.

------------------------------------------------------------------------

# 14. Advice Types

Advice            Purpose
  ----------------- ------------------------------------------------
@Before           Execute before method
@After            Execute after method
@AfterReturning   Execute only after successful completion
@AfterThrowing    Execute when an exception occurs
@Around           Execute before and after with complete control

------------------------------------------------------------------------

# 15. Real-world Applications

Spring uses AOP internally for many features, including:

-   `@Transactional`
-   Method Security
-   Performance Monitoring
-   Logging
-   Caching

------------------------------------------------------------------------

# Practice Exercises

Complete the following exercises:

1.  Print the controller method name.
2.  Print the current timestamp before every controller method.
3.  Measure API execution time using `@Around`.
4.  Print request arguments using `joinPoint.getArgs()`.
5.  Observe what happens if `joinPoint.proceed()` is removed.

------------------------------------------------------------------------

# Key Takeaways

-   AOP separates common functionality from business logic.
-   Logging is a Cross-Cutting Concern.
-   Spring applies AOP using Proxies.
-   `@Before` runs before a method.
-   `@Around` surrounds the method and controls its execution.
-   `ProceedingJoinPoint.proceed()` invokes the target method.
