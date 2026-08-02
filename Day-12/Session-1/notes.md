# Phase 2 -- Spring Boot

# Session 1 -- From Spring Framework to Spring Boot

> **Goal:** Understand why Spring Boot was introduced, how it differs
> from the Spring Framework, and create your first Spring Boot
> application.

------------------------------------------------------------------------

# Learning Objectives

By the end of this session, you will be able to:

-   Understand the limitations of traditional Spring applications.
-   Explain why Spring Boot was introduced.
-   Describe the architecture of Spring Boot.
-   Compare Spring Framework and Spring Boot.
-   Create your first Spring Boot project.
-   Explain the purpose of `@SpringBootApplication`.
-   Understand how an embedded Tomcat server starts automatically.

------------------------------------------------------------------------

# 1. Quick Recap of Phase 1

During Phase 1, we learned the core concepts of the Spring Framework.

Topics covered:

-   Spring Core (IoC & DI)
-   Bean Lifecycle
-   Bean Scopes
-   Annotation-Based Configuration
-   Spring JDBC
-   Spring MVC
-   Hibernate & JPA
-   REST APIs

A typical application looked like:

``` text
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

To make this work, we manually configured:

-   Maven dependencies
-   Component scanning
-   DispatcherServlet
-   View Resolver
-   DataSource
-   Transaction Manager
-   Tomcat deployment

------------------------------------------------------------------------

# 2. The Problem with Traditional Spring

Spring Framework is powerful and flexible, but creating a project
involved a lot of setup.

Typical workflow:

``` text
Create Maven Project
        ↓
Add Spring Dependencies
        ↓
Create AppConfig
        ↓
Configure Component Scan
        ↓
Configure DispatcherServlet
        ↓
Configure View Resolver
        ↓
Install Apache Tomcat
        ↓
Build WAR
        ↓
Deploy WAR
        ↓
Run Application
```

Notice that most of the work is configuration---not business logic.

## Major Problems

### Too Much Configuration

Many configuration classes and XML/Java configuration files.

### Dependency Management

Developers had to choose compatible versions manually.

### External Tomcat

Applications were packaged as WAR files and deployed separately.

### Boilerplate Code

Many projects contained nearly identical configuration.

### Slow Startup for New Projects

Even a simple REST API required significant setup.

------------------------------------------------------------------------

# 3. Why Spring Boot?

Developers wanted to spend more time solving business problems and less
time configuring infrastructure.

The Spring team observed that most applications required the same
configuration.

So they automated it.

This became **Spring Boot**.

------------------------------------------------------------------------

# 4. What is Spring Boot?

**Definition**

Spring Boot is an opinionated framework built on top of the Spring
Framework that helps developers create production-ready applications
with minimal configuration.

Think of it as:

``` text
Spring Boot = Spring Framework + Automation
```

Spring Boot is **not a replacement** for Spring Framework.

It is built **on top of Spring Framework**.

``` text
Application
      ↓
Spring Boot
      ↓
Spring Framework
      ↓
JVM
```

------------------------------------------------------------------------

# 5. Convention over Configuration

Spring Boot follows **Convention over Configuration**.

Instead of asking developers to configure everything manually, it
provides sensible defaults.

Example:

Instead of asking:

-   Which web server?
-   Which logging framework?
-   Which JSON library?

Spring Boot automatically configures them based on your dependencies.

You can still override the defaults whenever required.

------------------------------------------------------------------------

# 6. Spring Boot Architecture

``` text
+--------------------------------+
|        Your Application        |
| Controllers, Services, Repos   |
+--------------------------------+
               ↓
+--------------------------------+
|          Spring Boot           |
| Auto Configuration             |
| Starter Dependencies           |
| Embedded Server                |
| External Configuration         |
+--------------------------------+
               ↓
+--------------------------------+
|       Spring Framework         |
| IoC | DI | MVC | AOP | Tx      |
+--------------------------------+
               ↓
+--------------------------------+
|      Embedded Web Server       |
| Tomcat / Jetty / Undertow      |
+--------------------------------+
               ↓
+--------------------------------+
|              JVM               |
+--------------------------------+
```

------------------------------------------------------------------------

# 7. Spring Framework vs Spring Boot

| Feature | Spring Framework | Spring Boot |
|---------|------------------|-------------|
| Configuration | Manual | Auto Configuration |
| Server | External Tomcat | Embedded Tomcat |
| Deployment | WAR | Executable JAR |
| Dependency Management | Manual | Starter Dependencies |
| Boilerplate Code | More | Less |
| Project Setup | Slower | Faster |

Remember:

> Spring Boot makes Spring easier---it does not replace Spring.

------------------------------------------------------------------------

# 8. Creating Your First Spring Boot Project

Open **Spring Initializr**.

Choose:

-   Project: Maven
-   Language: Java
-   Java Version: 17 
-   Group: `com.training`
-   Artifact: `train-service`

Dependency:

-   Spring Web

Generate the project and import it into IntelliJ IDEA.

------------------------------------------------------------------------

# 9. Project Structure

``` text
train-service
│
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   │       └── application.properties
│   └── test
│
├── pom.xml
│
└── TrainServiceApplication.java
```

------------------------------------------------------------------------

# 10. TrainServiceApplication

``` java
@SpringBootApplication
public class TrainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainServiceApplication.class, args);
    }

}
```

This is the entry point of a Spring Boot application.

------------------------------------------------------------------------

# 11. Understanding @SpringBootApplication

`@SpringBootApplication` is a combination of three annotations.

``` text
@SpringBootApplication
        │
        ├── @Configuration
        ├── @EnableAutoConfiguration
        └── @ComponentScan
```

### @Configuration

Marks the class as a configuration class.

### @ComponentScan

Automatically scans components in the current package and subpackages.

### @EnableAutoConfiguration

Automatically configures Spring based on the dependencies available in
the project.

Example:

If Spring Web is present, Boot automatically configures:

-   DispatcherServlet
-   Jackson
-   Embedded Tomcat
-   Error handling

------------------------------------------------------------------------

# 12. What Happens When We Click Run?

``` text
Run main()
      ↓
SpringApplication.run()
      ↓
Create ApplicationContext
      ↓
Perform Component Scan
      ↓
Apply Auto Configuration
      ↓
Create Beans
      ↓
Start Embedded Tomcat
      ↓
Application Ready
```

Console output:

``` text
Tomcat started on port(s): 8080 (http)
Started TrainServiceApplication
```

------------------------------------------------------------------------

# 13. The Magic Moment

Question:

**Did we install Tomcat?**

No.

**Did we deploy a WAR file?**

No.

**Did we start Tomcat manually?**

No.

So who started Tomcat?

**Spring Boot did.**

Spring Boot downloads and packages an embedded Tomcat server inside the
application.

When the application starts, it automatically starts Tomcat.

------------------------------------------------------------------------

# 14. Why Embedded Tomcat?

Traditional Spring:

``` text
Build WAR
      ↓
Install Tomcat
      ↓
Deploy WAR
      ↓
Restart Tomcat
```

Spring Boot:

``` text
Run Application
      ↓
Embedded Tomcat Starts
      ↓
Application Ready
```

Benefits:

-   Faster development
-   Easier deployment
-   Portable executable JAR
-   No external server installation

------------------------------------------------------------------------

# 15. Hands-on Exercise

1.  Create a Spring Boot project using Spring Initializr.
2.  Add the Spring Web dependency.
3.  Import into IntelliJ IDEA.
4.  Run `TrainServiceApplication`.
5.  Observe the console output.
6.  Verify Tomcat starts on port 8080.

------------------------------------------------------------------------

# 16. Interview Questions

1.  Why was Spring Boot introduced?
2.  Is Spring Boot a replacement for Spring Framework?
3.  What is Auto Configuration?
4.  What is Convention over Configuration?
5.  What does `@SpringBootApplication` contain?
6.  What is the role of `SpringApplication.run()`?
7.  Why is Embedded Tomcat useful?
8.  Difference between WAR and executable JAR?

------------------------------------------------------------------------

# Key Takeaways

-   Spring Boot is built on top of Spring Framework.
-   Spring Boot reduces configuration.
-   Auto Configuration is one of its biggest features.
-   Starter dependencies simplify dependency management.
-   Embedded Tomcat removes the need for external server installation.
-   `@SpringBootApplication` combines three important annotations.
-   Spring Boot lets developers focus on business logic instead of
    infrastructure.
