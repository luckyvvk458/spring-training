# Phase 2 -- Spring Boot

# Session 2 -- Auto Configuration & Starter Dependencies

> Goal: Understand how Spring Boot removes configuration using Starter
> Dependencies, Auto Configuration, Dependency Management and Embedded
> Tomcat.

## Learning Objectives

-   Explain Starter Dependencies
-   Explain Auto Configuration
-   Explain Dependency Management
-   Explain Embedded Tomcat
-   Compare Traditional Spring and Spring Boot
-   Convert a Spring MVC application to Spring Boot

------------------------------------------------------------------------

# Traditional Spring vs Spring Boot

## Traditional Spring

Typical project:

``` text
SpringMVC
├── AppConfig
├── WebConfig
├── WebInitializer
├── Controller
├── Service
├── Repository
└── pom.xml
```

Configuration responsibilities:

-   AppConfig → Bean definitions
-   WebConfig → MVC configuration
-   WebInitializer → DispatcherServlet registration
-   External Tomcat → Application hosting

Typical flow:

``` text
Create Project
↓
Add Dependencies
↓
Create Config Classes
↓
Configure DispatcherServlet
↓
Configure View Resolver
↓
Install Tomcat
↓
Deploy WAR
↓
Run
```

## Spring Boot

``` text
TrainServiceApplication
Controller
Service
Repository
pom.xml
```

Run directly.

The framework configures everything automatically.

------------------------------------------------------------------------

# Starter Dependencies

Instead of adding many libraries manually:

-   spring-core
-   spring-context
-   spring-web
-   spring-webmvc
-   jackson
-   servlet-api

We simply add:

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

A Starter is a ready-made collection of compatible libraries.

Common starters:

Starter                        Purpose
  ------------------------------ ---------------
spring-boot-starter-web        REST/Web
spring-boot-starter-data-jpa   JPA/Hibernate
spring-boot-starter-security   Security
spring-boot-starter-test       Testing
spring-boot-starter-actuator   Monitoring

------------------------------------------------------------------------

# Dependency Management

Spring Boot manages library versions through:

``` xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
</parent>
```

Benefits:

-   Compatible library versions
-   Less maintenance
-   No version conflicts for common Spring libraries

------------------------------------------------------------------------

# Auto Configuration

Definition:

Spring Boot automatically configures beans based on the dependencies
available in the application.

Example:

If Boot finds:

``` text
spring-boot-starter-web
```

It configures:

``` text
DispatcherServlet
↓
Jackson
↓
Message Converters
↓
Spring MVC
↓
Embedded Tomcat
```

If Boot finds:

``` text
spring-boot-starter-data-jpa
```

It configures:

-   EntityManager
-   Hibernate
-   Transaction Manager
-   Repository support

Auto Configuration only applies when the required dependency exists.

------------------------------------------------------------------------

# Embedded Tomcat

With traditional Spring:

``` text
Build WAR
↓
Install Tomcat
↓
Deploy
↓
Restart
```

With Spring Boot:

``` text
Run Application
↓
Embedded Tomcat Starts
↓
Application Ready
```

Console:

``` text
Tomcat started on port(s): 8080 (http)
```

The web starter already includes Embedded Tomcat.

------------------------------------------------------------------------

# Hands-on

## Step 1

Replace multiple Spring dependencies with:

``` xml
spring-boot-starter-web
```

## Step 2

Delete:

-   AppConfig
-   WebConfig
-   WebInitializer

## Step 3

Create:

``` java
@SpringBootApplication
public class TrainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainServiceApplication.class, args);
    }

}
```

## Step 4

Run the application.

Observe:

``` text
Tomcat started on port(s): 8080 (http)
```

------------------------------------------------------------------------

# Traditional Spring vs Spring Boot

Traditional Spring        Spring Boot
  ------------------------- ----------------------
Manual Configuration      Auto Configuration
Individual Dependencies   Starter Dependencies
External Tomcat           Embedded Tomcat
WAR                       Executable JAR
Manual Versions           Managed Versions

------------------------------------------------------------------------

# Interview Questions

1.  What are Starter Dependencies?
2.  What is Auto Configuration?
3.  How does Embedded Tomcat work?
4.  What is the purpose of spring-boot-starter-parent?
5.  Can Auto Configuration be overridden?
6.  Why is Spring Boot easier than Spring Framework?

------------------------------------------------------------------------

# Key Takeaways

-   Starter Dependencies simplify dependency management.
-   Auto Configuration removes boilerplate configuration.
-   Embedded Tomcat eliminates external server installation.
-   Spring Boot manages compatible dependency versions.
-   Developers focus on business logic rather than infrastructure.
