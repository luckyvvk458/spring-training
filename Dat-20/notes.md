# JWT-Based Authentication and Authorization — Detailed Notes

## 1. Introduction

JWT stands for **JSON Web Token**. It is commonly used to secure REST APIs using token-based authentication.

In our John Doe / Train Service application:

```text
User Login
    ↓
Username + Password
    ↓
Spring Security authenticates the user
    ↓
JWT is generated
    ↓
Client receives JWT
    ↓
Client sends JWT with future requests
    ↓
JWT is validated
    ↓
User is authenticated for the current request
    ↓
Spring Security checks authorization
    ↓
Controller executes or access is denied
```

The best overall term is:

> **JWT-Based Authentication and Authorization**

---

# 2. Authentication vs Authorization

## Authentication — Who are you?

Authentication verifies the user's identity.

```text
Username: vivek
Password: password
        ↓
Credentials validated
        ↓
User authenticated
```

**Question:** Who is making this request?

---

## Authorization — What are you allowed to do?

Authorization checks what an authenticated user can access.

```text
vivek → ROLE_USER
admin → ROLE_ADMIN
```

Example:

```text
GET /trains/**       → USER or ADMIN
POST /trains/**      → ADMIN
PUT /trains/**       → ADMIN
DELETE /trains/**    → ADMIN
```

### Simple difference

```text
Authentication → Who are you?
Authorization  → What are you allowed to do?
```

---

# 3. JWT and Our Existing Project

Before JWT:

```text
Every protected request
        ↓
Username + Password
        ↓
Spring Security authenticates
        ↓
Authorization rules checked
```

With JWT:

```text
LOGIN
Username + Password
        ↓
Authenticate user
        ↓
Generate JWT
        ↓
Return JWT

---------------------------

FUTURE REQUESTS
Authorization: Bearer <JWT>
        ↓
Validate JWT
        ↓
Authenticate current request
        ↓
Check authorization
```

Important:

> **We are primarily changing the authentication mechanism. Our existing role-based authorization rules can remain.**

---

# 4. Three Phases

We can understand the complete flow in three phases:

```text
1. During Spring Boot Startup
2. During Login
3. During a Protected Request such as getAll
```

---

# PART 1 — DURING SPRING BOOT STARTUP

## 5. Startup Flow

```text
Spring Boot Starts
        ↓
ApplicationContext is created
        ↓
Configuration classes are processed
        ↓
SecurityConfig is processed
        ↓
Security infrastructure is configured
        ↓
Application is ready for requests
```

Three important concepts:

```text
UserDetailsService
AuthenticationManager
SecurityFilterChain
```

---

## 6. UserDetails and UserDetailsService

Suppose our users are:

```text
admin
password: admin123
role: ADMIN

vivek
password: password
role: USER
```

Conceptually:

```text
UserDetailsService
        │
        ├── UserDetails → admin
        │       └── ROLE_ADMIN
        │
        └── UserDetails → vivek
                └── ROLE_USER
```

A `UserDetails` object contains security information such as:

```text
Username
Password
Authorities / Roles
```

The `UserDetailsService` loads a user:

```java
userDetailsService.loadUserByUsername("vivek");
```

If the user is unavailable:

```text
UsernameNotFoundException
```

---

## 7. AuthenticationManager

The `AuthenticationManager` authenticates credentials.

```text
Username + Password
        ↓
AuthenticationManager
        ↓
Find User
        ↓
Validate Credentials
        ↓
Success or Failure
```

Examples:

```text
vivek + correct password → Success
vivek + wrong password   → Failure
nanda + password         → User not found / Failure
```

---

## 8. SecurityFilterChain

The `SecurityFilterChain` defines how requests are processed by Spring Security.

```text
Incoming HTTP Request
        ↓
Security Filter Chain
        ↓
Security checks
        ↓
Authentication
        ↓
Authorization
        ↓
Controller
```

We register our custom JWT filter in this chain:

```text
SecurityFilterChain
        │
        ├── Spring Security Filters
        ├── JwtAuthenticationFilter
        └── Other Filters
```

Typical registration:

```java
.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
)
```

Important:

> **During startup, the JWT filter is registered. It does not authenticate users during startup.**

---

## 9. Startup Summary

```text
SPRING BOOT STARTUP
        ↓
SecurityConfig processed
        ↓
UserDetailsService ready
        ↓
AuthenticationManager ready
        ↓
SecurityFilterChain configured
        ↓
JwtAuthenticationFilter registered
        ↓
Application ready
```

---

# PART 2 — DURING LOGIN

## 10. Login API

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "username": "vivek",
  "password": "password"
}
```

Flow:

```text
Client
   ↓
POST /auth/login
   ↓
AuthController
   ↓
AuthenticationManager
   ↓
UserDetailsService
   ↓
Credentials valid?
   ├── No  → Authentication fails
   └── Yes
          ↓
       JwtService
          ↓
       Generate JWT
          ↓
       Return token
```

The endpoint must be public:

```text
/auth/login → permitAll()
```

The user cannot send a JWT before logging in because they do not have one yet.

---

## 11. LoginRequest

```java
@Data
public class LoginRequest {

    private String username;
    private String password;
}
```

---

## 12. LoginResponse

```java
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
}
```

Example:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## 13. AuthController

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest loginRequest) {

    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            )
    );

    String token = jwtService.generateToken(
            loginRequest.getUsername()
    );

    return ResponseEntity
            .<LoginResponse>ok(new LoginResponse(token));
}
```

The most important sequence:

```text
AuthenticationManager.authenticate(...)
                ↓
Authentication successful?
                ↓
YES
                ↓
Generate JWT
```

Never generate a JWT before successfully validating credentials.

---

# PART 3 — THE THREE PARTS OF JWT

## 14. JWT Structure

A JWT has three parts separated by dots:

```text
HEADER.PAYLOAD.SIGNATURE
```

Example:

```text
xxxxx.yyyyy.zzzzz
  │     │     │
  │     │     └── Signature
  │     └──────── Payload
  └────────────── Header
```

---

## 15. Header

The header describes the token and signing algorithm.

Example:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

```text
alg → Signing algorithm
typ → Token type
```

Memory:

```text
Header → HOW?
```

---

## 16. Payload

The payload contains claims.

Example:

```json
{
  "sub": "vivek",
  "iat": 1780000000,
  "exp": 1780003600
}
```

```text
sub → Subject / username
iat → Issued At
exp → Expiration
```

Memory:

```text
Payload → WHO / WHAT?
```

Important:

> **JWT payload is generally encoded, not encrypted. Never put passwords or sensitive secrets in it.**

---

## 17. Signature

The signature protects the integrity of the token.

```text
Header
   +
Payload
   +
Secret Key
       ↓
Signing Algorithm
       ↓
Signature
```

If somebody changes:

```json
{
  "sub": "vivek"
}
```

to:

```json
{
  "sub": "admin"
}
```

the signature validation fails.

```text
Modified token
        ↓
Signature does not match
        ↓
JWT rejected
```

Memory:

```text
Signature → TRUST
```

---

## 18. JWT Memory Trick

```text
Header    → How was it created?
Payload   → What information does it contain?
Signature → Can I trust it?
```

---

# 19. JwtService

Example JWT generation:

```java
public String generateToken(String username) {

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    SecretKey key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );

    return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
}
```

Mapping:

```text
.subject(username) → sub claim
.issuedAt(now)     → iat claim
.expiration(...)   → exp claim
.signWith(key)     → creates signature
.compact()         → HEADER.PAYLOAD.SIGNATURE string
```

---

# 20. JWT Configuration

```properties
jwt.secret=my-super-secret-key-for-john-doe-railway-application-123456789
jwt.expiration=3600000
```

```text
jwt.secret     → signs/verifies token
jwt.expiration → token validity duration
```

```text
3,600,000 milliseconds
        ↓
1 hour
```

For production, secrets should be managed securely rather than hard-coded.

---

# PART 4 — DURING A PROTECTED getAll REQUEST

## 21. Example Request

Vivek already logged in and received a JWT.

Now:

```http
GET /trains
Authorization: Bearer <Vivek's JWT>
```

Flow:

```text
GET /trains
        ↓
SecurityFilterChain
        ↓
JwtAuthenticationFilter
        ↓
Extract JWT
        ↓
Extract username
        ↓
Check whether current request is already authenticated
        ↓
Load user
        ↓
Validate JWT
        ↓
Create Authentication
        ↓
Put it into SecurityContext
        ↓
Authorization check
        ↓
Controller
```

---

# 22. JwtAuthenticationFilter

Important code:

```java
if (username != null &&
        SecurityContextHolder.getContext().getAuthentication() == null) {

    UserDetails userDetails =
            userDetailsService.loadUserByUsername(username);

    if (jwtService.isTokenValid(token)) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
```

---

# 23. Understanding `username != null`

```java
username != null
```

Means:

> Were we able to extract a username from the JWT?

Example:

```text
JWT
  ↓
Extract username
  ↓
vivek
```

Then:

```text
vivek != null → true
```

---

# 24. Understanding `getAuthentication() == null`

```java
SecurityContextHolder.getContext().getAuthentication() == null
```

Means:

> **For this CURRENT HTTP request, has an Authentication object already been established?**

It does NOT mean:

```text
Has Vivek ever logged in before? ❌
```

It means:

```text
Has THIS request already been authenticated? ✅
```

---

# 25. Multiple Requests from the Same Person

Suppose Vivek sends:

```text
Request 1 → GET /trains
Request 2 → GET /trains/12730
Request 3 → GET /booking
```

Each is a separate HTTP request.

Conceptually:

```text
REQUEST 1
Authentication = null
        ↓
JWT authenticates Vivek
        ↓
Authentication = Vivek
        ↓
Request completes
```

Then:

```text
REQUEST 2
Authentication = null initially
        ↓
JWT authenticates Vivek
        ↓
Authentication = Vivek
```

Therefore, this check is **not about whether Vivek authenticated in a previous request**.

---

# 26. When Can `getAuthentication() != null`?

This happens when an Authentication object has already been created for the **current request**.

Example:

```text
ONE HTTP REQUEST
        ↓
Earlier authentication mechanism
        ↓
Authentication = Vivek
        ↓
JwtAuthenticationFilter
        ↓
getAuthentication() != null
```

Then JWT authentication is skipped.

Meaning:

> **This request is already authenticated. Do not authenticate it again.**

---

## Example with Multiple Authentication Mechanisms

```text
Request
   ↓
Session Authentication
   ↓
Authentication = Vivek
   ↓
JwtAuthenticationFilter
   ↓
Already authenticated
   ↓
Skip JWT authentication
```

The same idea can apply when another security mechanism has already populated the `SecurityContext`.

---

# 27. The Filter Itself Changes the Value

Before:

```text
SecurityContext
┌────────────────────┐
│ Authentication     │
│       null         │
└────────────────────┘
```

JWT is validated, then:

```java
SecurityContextHolder.getContext()
        .setAuthentication(authentication);
```

After:

```text
SecurityContext
┌────────────────────┐
│ Authentication     │
│       Vivek        │
└────────────────────┘
```

Now:

```text
getAuthentication() != null
```

because an Authentication object exists.

---

# 28. Simplest Interpretation

This:

```java
if (username != null &&
        SecurityContextHolder.getContext().getAuthentication() == null)
```

means:

```text
If:
    I know who the JWT belongs to
AND
    Nobody has authenticated this current request yet

Then:
    Authenticate this request using JWT
```

---

# 29. SecurityContextHolder

After successful JWT validation, the security context contains information such as:

```text
Principal    → Vivek
Authorities  → ROLE_USER
```

Conceptually:

```text
SecurityContext
        ↓
Authentication
        ├── Principal → vivek
        └── Authorities → ROLE_USER
```

Now Spring Security can authorize the request.

---

# 30. Creating the Authentication Object

```java
new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
);
```

Contains:

```text
Principal
    ↓
Current user

Credentials
    ↓
null

Authorities
    ↓
ROLE_USER / ROLE_ADMIN
```

Credentials are `null` because we are not storing the password for this request. JWT has already been validated.

---

# 31. Authentication vs Authorization During getAll

Request:

```http
GET /trains
Authorization: Bearer <JWT>
```

## Authentication

```text
JWT
  ↓
Validate signature
  ↓
Check expiration
  ↓
Extract username
  ↓
Load user
  ↓
Create Authentication
  ↓
SecurityContextHolder
```

Question:

```text
Who is this user?
```

Answer:

```text
Vivek
```

## Authorization

```text
Vivek
  ↓
ROLE_USER
  ↓
GET /trains
  ↓
Rule: USER or ADMIN
  ↓
Allowed
```

Question:

```text
Can Vivek access this API?
```

Answer:

```text
Yes
```

---

# 32. Full End-to-End Flow

## Phase 1 — Startup

```text
Spring Boot starts
        ↓
SecurityConfig processed
        ↓
UserDetailsService ready
        ↓
AuthenticationManager ready
        ↓
SecurityFilterChain configured
        ↓
JwtAuthenticationFilter registered
```

## Phase 2 — Login

```text
POST /auth/login
        ↓
Username + Password
        ↓
AuthenticationManager
        ↓
Credentials valid?
   ├── No  → Fail
   └── Yes
          ↓
       Generate JWT
          ↓
       Return token
```

## Phase 3 — Protected Request

```text
GET /trains
Authorization: Bearer <JWT>
        ↓
SecurityFilterChain
        ↓
JwtAuthenticationFilter
        ↓
Extract token
        ↓
Extract username
        ↓
Already authenticated?
   ├── Yes → Skip duplicate authentication
   └── No
          ↓
       Load user
          ↓
       Validate JWT
          ↓
       Create Authentication
          ↓
       SecurityContextHolder
          ↓
       Authorization
          ↓
       Controller
```

---

# 33. Example: Vivek

```text
Login:
vivek + password
        ↓
Authentication successful
        ↓
JWT generated
```

Later:

```http
GET /trains
Authorization: Bearer <Vivek JWT>
```

```text
Extract username → vivek
Authentication null? → Yes
Load vivek → Success
JWT valid? → Yes
Create Authentication
ROLE_USER
        ↓
GET /trains allowed
```

---

# 34. Example: Nanda Does Not Exist

Suppose a validly parsed token claims:

```text
username = nanda
```

Then:

```java
userDetailsService.loadUserByUsername("nanda");
```

Available users:

```text
admin
vivek
```

Result:

```text
Nanda not found
        ↓
Authentication cannot be established
        ↓
Protected request denied
```

Also, simply modifying a JWT payload does not work because the signature validation fails.

---

# 35. Stateless JWT Concept

JWT is commonly used with stateless session management.

```text
Server does not maintain a traditional session for the user
        ↓
Client sends JWT with each request
        ↓
Each request is independently authenticated
```

Important statement:

> **Login happens once to obtain the token, but JWT validation/authentication happens for every protected request.**

```text
Login once
    ↓
Receive JWT

Request 1 → Send JWT → Authenticate current request
Request 2 → Send JWT → Authenticate current request
Request 3 → Send JWT → Authenticate current request
```

---

# 36. Incremental Implementation Plan

## Step 1 — Generate JWT

```text
Login credentials
        ↓
Authenticate existing user
        ↓
Generate JWT
        ↓
Return token
```

## Step 2 — Read and Validate JWT

Add:

```text
extractUsername(token)
isTokenValid(token)
isTokenExpired(token)
```

## Step 3 — JwtAuthenticationFilter

```text
Request
  ↓
Read Authorization header
  ↓
Extract Bearer token
  ↓
Validate JWT
  ↓
Create Authentication
  ↓
SecurityContextHolder
```

## Step 4 — Replace HTTP Basic

Move from:

```text
Authorization: Basic ...
```

to:

```text
Authorization: Bearer <JWT>
```

## Step 5 — Reuse Existing Roles

```text
JWT
    ↓
Authentication

USER / ADMIN
    ↓
Authorization
```

---

# 37. Final Key Takeaways

## JWT

```text
HEADER.PAYLOAD.SIGNATURE
```

```text
Header    → How was it created?
Payload   → Who/what information?
Signature → Can it be trusted?
```

## Security

```text
Authentication → Who are you?
Authorization  → What can you do?
```

## Three Phases

```text
STARTUP
→ Prepare security infrastructure

LOGIN
→ Authenticate credentials
→ Generate JWT

PROTECTED REQUEST
→ Validate JWT
→ Establish Authentication
→ Check Authorization
→ Execute Controller
```

## Most Important Meaning

```java
SecurityContextHolder.getContext().getAuthentication() == null
```

means:

> **For this current request, no Authentication object has been established yet.**

It does not mean:

> **The user has never logged in before.**

And:

```text
getAuthentication() != null
```

means:

> **An Authentication object already exists for the current request, so we should not authenticate it again unnecessarily.**

---

# 38. Best Classroom Summary

> **During application startup, Spring prepares the security infrastructure and registers the JWT filter. During login, Spring validates the username and password and generates a JWT. During every protected request, the JWT filter validates the token and establishes authentication for that current request. Finally, Spring Security uses the user's roles and authorities to decide whether the requested API is authorized.**
