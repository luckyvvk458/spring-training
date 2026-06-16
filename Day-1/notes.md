# Day 1 - Java Fundamentals and Introduction to Spring

## What we are going to cover

* OOP Concepts
* Interfaces
* Enterprise Application Architecture
* Problems with Traditional Java Applications
* Why Spring?

---

# OOP Concepts

Before learning Spring, it is important to revisit some Java concepts because Spring is built on top of these ideas.

## Encapsulation

Encapsulation is the process of hiding data and providing controlled access to it.

```java
class BankAccount {

    private double balance;

    public void deposit(double amount) {
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
```

Why do we need it?

* Protect data from direct modification
* Better maintainability
* Better control over business rules

---

## Inheritance

Inheritance allows one class to reuse the properties and methods of another class.

```java
class Vehicle {

    void start() {
        System.out.println("Vehicle Started");
    }
}

class Car extends Vehicle {

}
```

Benefits:

* Code reuse
* Reduced duplication
* Easier maintenance

---

## Polymorphism

Polymorphism allows the same reference type to behave differently based on the object it points to.

```java
Vehicle vehicle = new Car();
vehicle.start();
```

Benefits:

* Flexible design
* Easier extension of applications

---

## Abstraction

Abstraction focuses on exposing only the required functionality and hiding implementation details.

```java
abstract class Vehicle {

    abstract void start();
}
```

Benefits:

* Cleaner design
* Better maintainability

---

## Interfaces

Interfaces define a contract that implementing classes must follow.

```java
interface Payment {
    void pay();
}
```

```java
class UPIPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through UPI");
    }
}
```

```java
class CardPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through Card");
    }
}
```

### Test Class

```java
public class Main {

    public static void main(String[] args) {

        Payment payment1 = new UPIPayment();
        payment1.pay();

        Payment payment2 = new CardPayment();
        payment2.pay();
    }
}
```

Output:

```text
Payment through UPI
Payment through Card
```

Notice that the `Main` class is working with the `Payment` interface and not directly with a specific implementation. The `Main` class only knows that a payment can be made by calling the `pay()` method. It does not need to know how the payment is processed internally.

This is one of the key benefits of interfaces and is heavily used in Spring to achieve loose coupling and Dependency Injection.

```java
interface Payment {
    void pay();
}
```

```java
class UPIPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through UPI");
    }
}
```

```java
class CardPayment implements Payment {

    @Override
    public void pay() {
        System.out.println("Payment through Card");
    }
}
```

Interfaces are heavily used in Spring and will become very important when we discuss Dependency Injection.

---

# Enterprise Application Architecture

Most enterprise applications follow a layered architecture.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller

Handles incoming requests and returns responses.

### Service

Contains business logic.

### Repository

Interacts with the database.

### Database

Stores application data.

---

# Problems in Traditional Java Applications

Let's consider a simple example.

```java
UserRepository repository =
        new UserRepository();

UserService service =
        new UserService(repository);
```

This looks fine for a small application.

Now imagine an application with:

* Hundreds of classes
* Multiple services
* Multiple repositories
* Logging
* Email services
* Database connections

Managing all these objects manually becomes difficult.

Some common challenges are:

## Manual Object Creation

```java
new UserRepository();
new UserService();
new EmailService();
new Logger();
```

## Tight Coupling

Classes become strongly dependent on each other.

## Difficult Testing

Replacing dependencies during testing becomes harder.

## Maintainability Issues

As the application grows, dependency management becomes complex.

---

# Why Spring?

Spring was introduced to simplify enterprise application development.

Instead of creating and managing objects manually, Spring takes responsibility for managing them.

Traditional Java:

```java
UserRepository repository =
        new UserRepository();

UserService service =
        new UserService(repository);
```

Spring:

```java
@Autowired
private UserService service;
```

Spring creates and injects the required dependencies automatically.

This concept is called Dependency Injection and is one of the core features of the Spring Framework.

---

# Summary

* OOP concepts form the foundation for Spring.
* Interfaces play an important role in application design.
* Enterprise applications are usually divided into layers.
* Managing dependencies manually becomes difficult as applications grow.
* Spring helps solve these problems using Dependency Injection and IoC.

---

# Next Session

* Introduction to Spring Framework
* IoC Container
* Dependency Injection
* Spring Beans
* Bean Lifecycle
* Creating the first Spring application
