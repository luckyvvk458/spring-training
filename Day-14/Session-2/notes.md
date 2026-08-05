# Spring Security - Session 1 Notes

## Authentication & Authorization using Spring Boot

> **Course Context:** These notes continue the Train Management
> application that we have been building throughout the Spring Boot
> course.

------------------------------------------------------------------------

# Learning Objectives

By the end of this session, you will be able to:

-   Understand why application security is important.
-   Explain why REST APIs must be protected.
-   Understand what Spring Security is.
-   Differentiate Authentication and Authorization.
-   Add Spring Security to a Spring Boot project.
-   Configure in-memory users.
-   Protect APIs based on roles.
-   Test secured APIs using Postman.
-   Understand the request flow inside Spring Security.

------------------------------------------------------------------------

# 1. The Problem Statement

Imagine our Train Management application exposes the following APIs:

HTTP Method   API            Purpose
  ------------- -------------- --------------
GET           /trains        View trains
GET           /trains/{id}   View train
POST          /trains        Add train
PUT           /trains/{id}   Update train
DELETE        /trains/{id}   Delete train

## Question

Can anyone on the Internet invoke these APIs?

Without security, the answer is **YES**.

Anyone could delete all train records.

Therefore, we need to **protect our APIs**.

------------------------------------------------------------------------

# 2. Why Protect APIs?

Different users should have different permissions.

User            Responsibilities
  --------------- ----------------------------------
Passenger       View trains
Booking Agent   Book tickets
Admin           Create, Update and Delete trains

Not every authenticated user should be allowed to perform every
operation.

This requirement introduces **authorization**.

------------------------------------------------------------------------

# 3. What is Spring Security?

Spring Security is a framework that protects Spring applications by
providing:

-   Authentication
-   Authorization
-   Protection against common attacks
-   Session management
-   Password encoding
-   CSRF protection
-   Integration with JWT and OAuth2

Think of Spring Security as a **security guard** standing in front of
your application.

    Client
       |
       v
    Spring Security
       |
       v
    Controller

Every request must pass through Spring Security before reaching your
controller.

------------------------------------------------------------------------

# 4. Authentication

Authentication answers:

> **Who are you?**

Example:

    Username : vivek
    Password : password

If credentials are valid:

    Authentication Successful

Otherwise:

    401 Unauthorized

------------------------------------------------------------------------

# 5. Authorization

Authorization answers:

> **What are you allowed to do?**

Example:

    User : vivek
    Role : USER

Trying to call:

    DELETE /trains/101

Spring checks whether the user has **ROLE_ADMIN**.

Result:

    403 Forbidden

------------------------------------------------------------------------

# Authentication vs Authorization

Authentication        Authorization
  --------------------- ------------------------------
Who are you?          What can you do?
Username & Password   Roles & Permissions
Happens first         Happens after authentication
Failure → 401         Failure → 403

------------------------------------------------------------------------

# 6. Airport Analogy

## Authentication

Security checks your passport.

Question:

"Are you really Vivek?"

If yes, you enter the airport.

## Authorization

Now you try entering the pilot cabin.

Passenger?

❌ No

Pilot?

✅ Yes

You were authenticated, but not authorized.

------------------------------------------------------------------------

# 7. Adding Spring Security

## Maven Dependency

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Restart the application.

Immediately all endpoints become protected.

------------------------------------------------------------------------

# 8. Default Behaviour

Browser:

    GET /trains

Result:

    Spring Login Page

Postman:

    401 Unauthorized

Why?

The browser supports form login.

Postman is an API client.

------------------------------------------------------------------------

# 9. Configuring Security

Create:

    src/main/java/.../security/SecurityConfig.java

``` java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.GET,"/trains/**")
                    .hasAnyRole("USER","ADMIN")

                .requestMatchers(HttpMethod.POST,"/trains/**")
                    .hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT,"/trains/**")
                    .hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE,"/trains/**")
                    .hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
```

------------------------------------------------------------------------

# 10. In-Memory Users

``` java
@Bean
UserDetailsService userDetailsService(){

    UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build();

    UserDetails user = User.builder()
            .username("vivek")
            .password("{noop}password")
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(admin,user);
}
```

### Why `{noop}`?

Spring Security expects encoded passwords.

`{noop}` tells Spring not to encode the password.

In production, use `BCryptPasswordEncoder`.

------------------------------------------------------------------------

# 11. Role-Based Authorization

Endpoint          USER   ADMIN
  ---------------- ------ -------
GET /trains        ✅     ✅
POST /trains       ❌     ✅
PUT /trains        ❌     ✅
DELETE /trains     ❌     ✅

------------------------------------------------------------------------

# 12. Testing with Postman

## USER

    Username : vivek
    Password : password

DELETE:

    403 Forbidden

Reason:

Authenticated ✔

Authorized ✘

------------------------------------------------------------------------

## ADMIN

    Username : admin
    Password : admin123

DELETE:

    200 OK

------------------------------------------------------------------------

# 13. Understanding the Request Flow

    HTTP Request
          |
          v
    Spring Security Filter Chain
          |
          +--> Authentication
          |        |
          |        +--> Invalid -> 401
          |
          +--> Authorization
          |        |
          |        +--> No Permission -> 403
          |
          v
    Controller
          |
          v
    Service
          |
          v
    Repository
          |
          v
    Database

If Spring Security rejects the request, the controller is never
executed.

------------------------------------------------------------------------

# 14. What is a Filter?

A filter runs **before** the controller.

    Client
       |
    Filter
       |
    Controller

Spring Security internally uses a chain of filters.

You configure them through `SecurityFilterChain`.

------------------------------------------------------------------------

# 15. What is SecurityFilterChain?

You are **not creating filters**.

You are configuring Spring Security.

Think of it as instructions:

-   Protect requests.
-   Require authentication.
-   Apply role-based authorization.
-   Use HTTP Basic Authentication.
-   Disable CSRF for REST APIs.

Spring Security builds the filter chain automatically.

------------------------------------------------------------------------

# 16. HTTP Basic Authentication

Postman sends:

    Authorization: Basic dml2ZWs6cGFzc3dvcmQ=

Spring:

1.  Reads the Authorization header.
2.  Decodes Base64.
3.  Extracts username and password.
4.  Validates credentials.
5.  Creates an authenticated user.
6.  Checks roles.

> Base64 is encoding, not encryption. Always use HTTPS with Basic
> Authentication.

------------------------------------------------------------------------

# 17. Key HTTP Status Codes

Code   Meaning
  ------ ---------------------------------------------------
200    Success
401    Authentication failed
403    Authentication succeeded but authorization failed

------------------------------------------------------------------------

# Session Summary

✔ Why Security?

✔ Why protect APIs?

✔ Spring Security

✔ Authentication

✔ Authorization

✔ SecurityFilterChain

✔ Filters

✔ In-Memory Users

✔ Role-Based Authorization

✔ HTTP Basic Authentication

✔ 401 vs 403

------------------------------------------------------------------------

# What's Next?

In the next session we will learn:

-   Password Encoding (BCrypt)
-   CSRF
-   Database Authentication
-   Custom UserDetailsService
-   JWT Authentication
