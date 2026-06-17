# Spring Framework - Day 2 Notes

## Learning Objectives

By the end of this session, participants should be able to:

* Understand why Spring Framework is required
* Explain Tight Coupling and Loose Coupling
* Understand Dependency Injection (DI)
* Understand Inversion of Control (IoC)
* Understand Spring Container and Bean Management
* Create and configure Spring Beans
* Use @Component, @Autowired, @Configuration and @ComponentScan
* Understand Bean Scopes
* Understand Bean Lifecycle

---

# Session 1 - Maven Refresher and Why Spring?

## Maven Refresher

### What is Maven?

Maven is a build and dependency management tool used in Java projects.

### Why Maven?

Without Maven:

* Manual JAR downloads
* Classpath management
* Version conflicts

With Maven:

* Automatic dependency management
* Standard project structure
* Build automation
* Packaging support

### pom.xml

```xml
<groupId>com.training</groupId>
<artifactId>spring-day2-training</artifactId>
<version>1.0-SNAPSHOT</version>
```

### Spring Dependency

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.2.0</version>
</dependency>
```

---

## Why Spring?

Large applications contain hundreds of classes.

Managing object creation manually becomes difficult.

Spring helps by:

* Creating objects
* Managing objects
* Injecting dependencies
* Managing lifecycle

---

## Tight Coupling

Example:

```java
private Engine engine = new Engine();
```

Problems:

* Car creates Engine
* Difficult to replace implementation
* Difficult to test
* Difficult to maintain

---

## Dependency

A dependency is an object required by another object.

Example:

```java
private Engine engine;
```

Car depends on Engine.

---

## Dependency Injection

Instead of:

```java
private Engine engine = new Engine();
```

Use:

```java
Engine engine = new Engine();
Car car = new Car(engine);
```

Definition:

Dependency Injection is a design pattern in which an object receives its dependencies from an external source.

---

## Inversion of Control (IoC)

Before Spring:

Application creates objects.

After Spring:

Spring creates objects.

Definition:

Inversion of Control is the process of transferring object creation and management responsibilities from application code to Spring Container.

---

## IoC vs DI

IoC:

Who creates objects?

Answer:

Spring Container

DI:

How are dependencies provided?

Answer:

@Autowired, Constructor Injection, Setter Injection

---

# Session 2 - Spring Container and Bean Management

## Spring Container

Responsibilities:

* Create Beans
* Manage Beans
* Inject Dependencies
* Manage Lifecycle

---

## Bean

Any object managed by Spring is called a Bean.

---

## @Component

```java
@Component
public class Engine {
}
```

Marks a class as a Spring Bean.

---

## @Autowired

```java
@Autowired
private Engine engine;
```

Performs Dependency Injection.

---

## @Configuration

```java
@Configuration
public class AppConfig {
}
```

Marks configuration class.

---

## @ComponentScan

```java
@ComponentScan("com.training")
```

Scans package for Spring Beans.

---

## ApplicationContext

```java
AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(AppConfig.class);
```

Creates Spring Container.

---

## getBean()

```java
Car car = context.getBean(Car.class);
```

Retrieves bean from container.

---

## Loose Coupling using Interfaces

```java
public interface Engine {
    void start();
}
```

```java
@Component
public class PetrolEngine implements Engine {
}
```

```java
@Component
public class DieselEngine implements Engine {
}
```

```java
@Autowired
private Engine engine;
```

Benefits:

* Easy implementation replacement
* Better maintainability
* Better testability

---

# Session 3 - Bean Scope

## What is Bean Scope?

Bean Scope determines:

* Number of objects created
* Lifetime of objects

---

## Singleton Scope

Default Scope

```java
@Scope("singleton")
```

Characteristics:

* One object per Spring Container
* Shared across application

---

## Prototype Scope

```java
@Scope("prototype")
```

Characteristics:

* New object per request
* Multiple instances

---

## Scope Comparison

| Scope     | Objects Created |
| --------- | --------------- |
| Singleton | One             |
| Prototype | Multiple        |

---

## Real World Examples

Singleton:

* Office Printer
* Company Email Server

Prototype:

* Employee ID Card
* Bank Account Passbook

---

# Session 4 - Bean Lifecycle

## Bean Lifecycle Flow

Bean Creation
↓
Dependency Injection
↓
@PostConstruct
↓
Business Usage
↓
@PreDestroy

---

## Constructor

```java
public Engine() {
    System.out.println("Constructor Called");
}
```

---

## @PostConstruct

```java
@PostConstruct
public void init() {
    System.out.println("Initialization");
}
```

Executed after dependency injection.

---

## @PreDestroy

```java
@PreDestroy
public void destroy() {
    System.out.println("Cleanup");
}
```

Executed before bean destruction.

---

# End-of-Day Assessment

### Theory Questions

1. What is Maven?
2. Why do we need Spring Framework?
3. What is Tight Coupling?
4. What is Dependency Injection?
5. What is IoC?
6. Difference between IoC and DI?
7. What is Spring Container?
8. What is a Bean?
9. What is @Component?
10. What is @Autowired?
11. What is Bean Scope?
12. Difference between Singleton and Prototype?
13. What is Bean Lifecycle?

### Coding Assignments

#### Assignment 1

Convert Employee-Laptop example from Tight Coupling to Dependency Injection.

#### Assignment 2

Create:

* PaymentProcessor
* CreditCardPayment
* UPIPayment
* ShoppingCart

Use Spring Dependency Injection.

#### Assignment 3

Create:

* Vehicle
* Car
* Bike

Demonstrate Loose Coupling using Interfaces.

#### Assignment 4

Create Student bean and demonstrate:

* Singleton Scope
* Prototype Scope

#### Assignment 5

Create DatabaseConnection bean and demonstrate:

* Constructor
* @PostConstruct
* @PreDestroy
