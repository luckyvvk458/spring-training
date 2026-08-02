# Phase 2 -- Spring Boot

# Session 4 -- application.properties & Spring Profiles

> **Goal:** Learn how Spring Boot manages application configuration
> using `application.properties` and how Spring Profiles help us run the
> same application in different environments.

------------------------------------------------------------------------

# Learning Objectives

By the end of this session, you will be able to:

-   Understand externalized configuration.
-   Use `application.properties`.
-   Configure server, logging and application settings.
-   Read values using `@Value`.
-   Understand Spring Profiles.
-   Create Development and Production profiles.
-   Run applications with different configurations.

------------------------------------------------------------------------

# 1. Why Do We Need Configuration?

Imagine a banking application.

During development we use:

-   Local database
-   Debug logging
-   Port 8080

In production we use:

-   Production database
-   Error logging
-   Port 9090

Should we change Java code every time?

**No.**

Configuration should be kept outside the source code.

This is called **Externalized Configuration**.

------------------------------------------------------------------------

# 2. What is application.properties?

`application.properties` is the default configuration file used by
Spring Boot.

Location:

``` text
src
└── main
    └── resources
        └── application.properties
```

Spring Boot automatically loads this file during application startup.

------------------------------------------------------------------------

# 3. Common Properties

## Change Server Port

``` properties
server.port=9090
```

Default:

``` properties
server.port=8080
```

------------------------------------------------------------------------

## Change Application Name

``` properties
spring.application.name=train-service
```

------------------------------------------------------------------------

## Configure Context Path

``` properties
server.servlet.context-path=/api
```

Now:

    http://localhost:8080/api/trains

------------------------------------------------------------------------

## Logging Level

``` properties
logging.level.root=INFO
logging.level.com.training=DEBUG
```

------------------------------------------------------------------------

## Banner

Disable startup banner.

``` properties
spring.main.banner-mode=off
```

------------------------------------------------------------------------

# 4. Database Properties

Example:

``` properties
spring.datasource.url=jdbc:mysql://localhost:3306/training
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Spring Boot automatically creates the DataSource.

------------------------------------------------------------------------

# 5. Reading Properties

Suppose:

``` properties
app.company=OpenAI Training
```

Read using `@Value`.

``` java
@RestController
public class WelcomeController {

    @Value("${app.company}")
    private String company;

    @GetMapping("/company")
    public String company() {
        return company;
    }

}
```

Output:

    OpenAI Training

------------------------------------------------------------------------

# 6. Environment-Specific Configuration

Different environments require different settings.

``` text
Development
↓

Testing
↓

Production
```

Instead of editing one properties file repeatedly, Spring Boot uses
**Profiles**.

------------------------------------------------------------------------

# 7. What is a Profile?

A Profile represents a specific runtime environment.

Common profiles:

-   dev
-   test
-   qa
-   prod

Each profile has its own configuration.

------------------------------------------------------------------------

# 8. Profile Files

``` text
resources
│
├── application.properties
├── application-dev.properties
├── application-test.properties
└── application-prod.properties
```

------------------------------------------------------------------------

# 9. Development Profile

``` properties
server.port=8081
logging.level.root=DEBUG
spring.datasource.url=jdbc:mysql://localhost:3306/devdb
```

------------------------------------------------------------------------

# 10. Production Profile

``` properties
server.port=9090
logging.level.root=ERROR
spring.datasource.url=jdbc:mysql://prod-server:3306/proddb
```

------------------------------------------------------------------------

# 11. Activating a Profile

Inside:

``` properties
application.properties
```

``` properties
spring.profiles.active=dev
```

Spring Boot loads:

-   application.properties
-   application-dev.properties

If changed to:

``` properties
spring.profiles.active=prod
```

Spring Boot loads production settings.

------------------------------------------------------------------------

# 12. Profile Loading Order

``` text
application.properties
          +
application-dev.properties
          ↓
Final Configuration
```

Profile-specific properties override common properties.

------------------------------------------------------------------------

# 13. Real-World Example

## Development

``` properties
server.port=8081
logging.level.root=DEBUG
```

## Production

``` properties
server.port=9090
logging.level.root=ERROR
```

Same code.

Different configuration.

No recompilation required.

------------------------------------------------------------------------

# 14. Hands-on Exercise

Create:

``` text
application.properties
application-dev.properties
application-prod.properties
```

Configure different:

-   Ports
-   Application Names
-   Logging Levels

Run using:

``` properties
spring.profiles.active=dev
```

Observe the port.

Change to:

``` properties
spring.profiles.active=prod
```

Observe the changes.

------------------------------------------------------------------------

# 15. Best Practices

-   Never hardcode environment values.
-   Keep secrets outside source control.
-   Use profiles for environment-specific settings.
-   Keep common settings in `application.properties`.

------------------------------------------------------------------------

# Interview Questions

1.  What is `application.properties`?
2.  Why do we externalize configuration?
3.  What is `@Value`?
4.  What is a Spring Profile?
5.  How do you activate a profile?
6.  Difference between `application.properties` and
    `application-dev.properties`?
7.  Which file has higher priority?
8.  Why are Profiles useful?

------------------------------------------------------------------------

# Key Takeaways

-   `application.properties` stores application configuration.
-   Spring Boot loads it automatically.
-   `@Value` reads property values.
-   Profiles help manage different environments.
-   Profile-specific files override common configuration.
-   Externalized configuration keeps code clean and portable.
