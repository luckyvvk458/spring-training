# Spring Framework - Day 3 Notes

## Learning Objectives

By the end of this session, participants should be able to:

* Understand Annotation-Based Configuration
* Understand the purpose of @Component, @Service and @Repository
* Understand Layered Architecture in Spring Applications
* Understand Java-Based Configuration
* Understand the purpose of @Bean
* Differentiate between @Component and @Bean
* Create and manage beans using Java Configuration
* Build a Mini Spring Core Application using all concepts learned so far

---

# Session 1 - Annotation-Based Configuration

## Recap

So far we have learned:

* Maven
* Spring Framework
* IoC
* Dependency Injection
* Spring Container
* Beans
* Bean Lifecycle
* Bean Scope
* @Component
* @Autowired
* @Configuration
* @ComponentScan

---

## What is Annotation-Based Configuration?

Annotation-Based Configuration is the process of configuring Spring applications using annotations instead of XML configuration files.

Instead of writing:

```xml
<bean id="engine"
      class="com.training.DieselEngine"/>
```

We can write:

```java
@Component
public class DieselEngine {
}
```

Spring automatically detects and creates the bean.

---

## Why Annotation-Based Configuration?

Problems with XML:

* Large XML files
* Difficult maintenance
* More configuration
* Harder to understand

Benefits of Annotations:

* Less configuration
* Cleaner code
* Easy maintenance
* Better readability

---

## How Spring Finds Beans?

```java
@Configuration
@ComponentScan("com.training")
public class AppConfig {
}
```

Flow:

Application Starts

↓

Component Scan Begins

↓

Spring Searches Packages

↓

Finds @Component Classes

↓

Creates Beans

↓

Stores Beans in Spring Container

---

## Experiment 1

Create:

```java
@Component
public class Engine {
}
```

Retrieve bean from container.

Observe successful bean creation.

---

## Experiment 2

Remove:

```java
@Component
```

Run application again.

Observe:

```text
NoSuchBeanDefinitionException
```

Discussion:

Spring only manages classes marked as components.

---

## Experiment 3

Move component outside scan package.

Observe bean discovery failure.

Discussion:

Component scanning scope is important.

---

# Session 2 - @Component, @Service and @Repository

## Problem Statement

If @Component can create beans, why do we need:

* @Service
* @Repository

---

## @Component

Generic Spring Bean.

Used for utility classes and helper classes.

Example:

```java
@Component
public class NotificationService {
}
```

---

## @Service

Represents Business Logic Layer.

Example:

```java
@Service
public class StudentService {
}
```

Responsibilities:

* Business rules
* Validation
* Processing
* Coordination between layers

---

## @Repository

Represents Persistence Layer.

Example:

```java
@Repository
public class StudentRepository {
}
```

Responsibilities:

* Database operations
* CRUD operations
* Data retrieval

---

## Layered Architecture

```text
Controller
    ↓
Service
    ↓
Repository
```

---

## Can @Component Replace @Service?

Yes.

Example:

```java
@Component
public class StudentService {
}
```

Application works successfully.

---

## Can @Component Replace @Repository?

Yes.

Example:

```java
@Component
public class StudentRepository {
}
```

Application works successfully.

---

## Then Why Use @Service and @Repository?

Benefits:

* Better readability
* Clear architecture
* Easy maintenance
* Easier onboarding of developers
* Layer identification

---

## Summary

| Annotation  | Purpose           |
| ----------- | ----------------- |
| @Component  | Generic Bean      |
| @Service    | Business Layer    |
| @Repository | Persistence Layer |

---

# Session 3 - Java Config and @Bean

## Problem Statement

We can create beans using:

```java
@Component
```

Then why do we need:

```java
@Bean
```

---

## Understanding the Problem

Suppose we have:

```java
public class ObjectMapper {
}
```

Can we add:

```java
@Component
public class ObjectMapper {
}
```

No.

Reason:

We do not own the source code.

---

## Real World Examples

Third-party classes:

* ObjectMapper
* DataSource
* JdbcTemplate
* ExecutorService
* RestTemplate
* KafkaProducer

These classes are provided by libraries.

We cannot modify them.

---

## What is Java Configuration?

Instead of XML configuration:

```xml
<bean id="engine"
      class="DieselEngine"/>
```

We use Java code.

Example:

```java
@Configuration
public class AppConfig {
}
```

---

## What is @Bean?

@Bean tells Spring:

Create and manage the returned object as a Spring Bean.

Example:

```java
@Bean
public Engine engine() {

    return new DieselEngine();
}
```

---

## Complete Example

### Engine

```java
public interface Engine {

    void start();
}
```

### DieselEngine

```java
public class DieselEngine
        implements Engine {

    @Override
    public void start() {

        System.out.println(
                "Diesel Engine Started");
    }
}
```

### Car

```java
@Component
public class Car {

    @Autowired
    private Engine engine;

    public void drive() {

        engine.start();

        System.out.println(
                "Car Started");
    }
}
```

### AppConfig

```java
@Configuration
@ComponentScan("com.training")
public class AppConfig {

    @Bean
    public Engine engine() {

        return new DieselEngine();
    }
}
```

### Main Class

```java
AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(
                AppConfig.class);

Car car = context.getBean(Car.class);

car.drive();
```

Output:

```text
Diesel Engine Started
Car Started
```

---

## @Component vs @Bean

| Feature                     | @Component          | @Bean               |
| --------------------------- | ------------------- | ------------------- |
| Applied On                  | Class               | Method              |
| Bean Creation               | Automatic           | Explicit            |
| Requires Source Code Access | Yes                 | No                  |
| Component Scanning          | Required            | Not Required        |
| Common Usage                | Application Classes | Third-Party Classes |

---

## Simple Rule

If I own the class:

```java
@Component
@Service
@Repository
```

If I do not own the class:

```java
@Bean
```

---

# Session 4 - Mini Spring Core Application

## Project: Student Course Registration System

### Layers

```text
StudentRepository
        ↓
StudentService
        ↓
NotificationService
```

---

## Annotations Used

```java
@Repository
```

```java
@Service
```

```java
@Component
```

```java
@Configuration
```

```java
@Bean
```

```java
@Autowired
```

---

## Learning Outcome

Students should understand:

* Layered Architecture
* Bean Creation
* Dependency Injection
* Component Scanning
* Java Configuration
* Bean Management

---

# End-of-Day Assessment

## Theory Questions

1. What is Annotation-Based Configuration?
2. Why are annotations preferred over XML?
3. What is the purpose of @Component?
4. What is the purpose of @Service?
5. What is the purpose of @Repository?
6. Can @Component replace @Service?
7. Why do we need @Bean?
8. Difference between @Component and @Bean?
9. What is Java Configuration?
10. When should @Bean be preferred?

---

## Coding Assignment

Create:

* BookRepository
* BookService
* NotificationService
* AppConfig
* Main Class

Requirements:

* Use @Repository
* Use @Service
* Use @Component
* Use @Bean
* Use @Autowired
* Use ApplicationContext

Expected Output:

```text
Saving Book Details

Notification Sent

Book Added Successfully
```

---

# Hands-On Exercise - Student Registration System

## Objective

Build a simple Student Registration System using:

* `@Repository`
* `@Service`
* `@Component`
* `@Autowired`
* `@Configuration`
* `@ComponentScan`
* `@Bean`
* `ApplicationContext`

---

## Project Structure

```text
com.training

├── config
│      AppConfig.java
│
├── model
│      Student.java
│      Course.java
│
├── repository
│      StudentRepository.java
│
├── service
│      StudentService.java
│
├── utility
│      NotificationService.java
│
└── main
       SpringApplication.java
```

---

## Step 1 - Create Student Model

```java
public class Student {

    private int id;
    private String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

---

## Step 2 - Create Course Model

This class will be managed using `@Bean`.

```java
public class Course {

    private String courseName;

    public Course(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseName() {
        return courseName;
    }
}
```

---

## Step 3 - Create StudentRepository

```java
@Repository
public class StudentRepository {

    public void save(Student student) {

        System.out.println(
                "Saving Student : "
                        + student.getName());
    }
}
```

### Why Repository?

Repository layer is responsible for database operations.

Examples:

* Insert
* Update
* Delete
* Retrieve

---

## Step 4 - Create NotificationService

```java
@Component
public class NotificationService {

    public void sendNotification(String name) {

        System.out.println(
                "Notification Sent To "
                        + name);
    }
}
```

### Why Component?

This class is neither:

* Service Layer
* Repository Layer

It is simply a utility/helper class.

---

## Step 5 - Create StudentService

```java
@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private NotificationService notificationService;

    public void registerStudent(Student student) {

        repository.save(student);

        notificationService
                .sendNotification(
                        student.getName());

        System.out.println(
                "Student Registered Successfully");
    }
}
```

### Why Service?

Service Layer contains business logic.

This class coordinates:

* Repository Layer
* Notification Layer

---

## Step 6 - Create AppConfig

```java
@Configuration
@ComponentScan("com.training")
public class AppConfig {

    @Bean
    public Course course() {

        return new Course(
                "Spring Framework");
    }
}
```

### Why @Bean?

Course is intentionally created using `@Bean` to demonstrate Java Configuration.

In real projects, `@Bean` is commonly used for:

* DataSource
* ObjectMapper
* RestTemplate
* ExecutorService
* KafkaProducer

---

## Step 7 - Create Main Class

```java
public class SpringApplication {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(
                        AppConfig.class);

        StudentService studentService =
                context.getBean(
                        StudentService.class);

        Student student =
                new Student(
                        101,
                        "Rahul");

        studentService
                .registerStudent(student);

        Course course =
                context.getBean(
                        Course.class);

        System.out.println(
                "Course Name : "
                        + course.getCourseName());

        context.close();
    }
}
```

---

## Expected Output

```text
Saving Student : Rahul

Notification Sent To Rahul

Student Registered Successfully

Course Name : Spring Framework
```

---

## Understanding Dependency Injection

### Dependency Relationship

```text
StudentService
      |
      +----> StudentRepository
      |
      +----> NotificationService
```

StudentService cannot complete student registration on its own.

It requires:

* StudentRepository
* NotificationService

These are called dependencies.

---

### Traditional Java Approach

```java
public class StudentService {

    private StudentRepository repository =
            new StudentRepository();

    private NotificationService notificationService =
            new NotificationService();
}
```

Problems:

* Tight Coupling
* Difficult to test
* Difficult to maintain
* Object creation remains inside the class

---

### Spring Dependency Injection

```java
@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private NotificationService notificationService;
}
```

Spring creates and injects the dependencies.

---

## Concepts Covered

| Concept              | Usage               |
| -------------------- | ------------------- |
| @Repository          | StudentRepository   |
| @Service             | StudentService      |
| @Component           | NotificationService |
| @Autowired           | StudentService      |
| @Configuration       | AppConfig           |
| @ComponentScan       | AppConfig           |
| @Bean                | Course              |
| ApplicationContext   | Main Class          |
| Dependency Injection | StudentService      |
| Bean Management      | Spring Container    |

---


