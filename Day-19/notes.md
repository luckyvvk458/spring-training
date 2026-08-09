# Spring Boot Caching with `@Cacheable` and TTL using Caffeine

## 1. Goal

This hands-on builds caching incrementally in the existing Train Service Client.

```text
Request data from John Doe
        ↓
Cache the response
        ↓
Verify later requests use cache
        ↓
Understand stale data
        ↓
Add TTL using Caffeine
        ↓
After expiry, fetch fresh data
```

---

# 2. Current Architecture

We have two applications.

```text
Train Service Client :8081
        ↓
TrainController
        ↓
RailwayClient
        ↓
RestClient
        ↓ HTTP
John Doe Railway :8080
        ↓
Train Service
        ↓
Repository
        ↓
MySQL
```

The client calls John Doe and receives `List<Train>`.

---

# 3. Why Cache?

Without caching:

```text
Request 1 → Call John Doe → Get trains
Request 2 → Call John Doe → Get the same trains
Request 3 → Call John Doe → Get the same trains
```

With caching:

```text
First Request
    ↓
CACHE MISS
    ↓
Call John Doe
    ↓
Store result in cache

Later Requests
    ↓
CACHE HIT
    ↓
Return cached result
```

---

# 4. Step 1: Add Spring Cache

Add to the client application's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

At this stage:

- No Caffeine
- No Redis
- No TTL

Our first goal is simply to prove that repeated requests are served from cache.

---

# 5. Step 2: Enable Caching

Add `@EnableCaching` to the main application class:

```java
@SpringBootApplication
@EnableCaching
public class DemoTrainServiceClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                DemoTrainServiceClientApplication.class,
                args
        );
    }
}
```

This enables support for caching annotations such as:

```text
@Cacheable
@CachePut
@CacheEvict
```

For now, we only use `@Cacheable`.

---

# 6. Step 3: Add `@Cacheable`

Example:

```java
@Cacheable("allTrains")
@Retry(name = "johnDoe")
public List<Train> findAllTrains() {

    System.out.println("Calling John Doe...");

    return restClient
            .get()
            .uri("/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {});
}
```

The cache name is:

```text
allTrains
```

Conceptually:

```text
Cache Name: allTrains
Key: SimpleKey []
Value: List<Train>
```

---

# 7. Why `SimpleKey []`?

The method has no parameters:

```java
public List<Train> findAllTrains()
```

Therefore Spring generates a key representing an empty argument list:

```text
SimpleKey []
```

Conceptually:

```text
allTrains
    │
    └── SimpleKey []
            │
            └── List<Train>
```

---

# 8. How to See Cache HIT and MISS

Do not put a `CACHE HIT` print inside the cached method.

```java
@Cacheable("allTrains")
public List<Train> findAllTrains() {
    System.out.println("CACHE HIT"); // Incorrect
}
```

On a cache hit, the method does not execute at all.

Enable Spring cache logging:

```properties
logging.level.org.springframework.cache=TRACE
```

---

# 9. First Request: Cache MISS

Restart the client application so the in-memory cache starts empty.

Call:

```text
GET http://localhost:8081/trains
```

Expected output:

```text
Calling John Doe...
```

Spring may log:

```text
Creating cache entry for key 'SimpleKey []' in cache(s) [allTrains]
```

Flow:

```text
Request
   ↓
Check Cache
   ↓
CACHE MISS
   ↓
Method executes
   ↓
Calling John Doe...
   ↓
Get List<Train>
   ↓
Create cache entry
```

---

# 10. Second Request: Cache HIT

Call the same endpoint again:

```text
GET http://localhost:8081/trains
```

Expected log:

```text
Cache entry for key 'SimpleKey []' found in cache(s) [allTrains]
```

Flow:

```text
Request
   ↓
Check Cache
   ↓
Data found
   ↓
CACHE HIT
   ↓
Return cached List<Train>
```

You should not see:

```text
Calling John Doe...
```

This proves the response came from cache.

---

# 11. Successful First Experiment

```text
Request 1
    ↓
CACHE MISS
    ↓
Calling John Doe...
    ↓
Store response

Request 2
    ↓
CACHE HIT
    ↓
Return cached response
    ↓
John Doe is NOT called

Request 3
    ↓
CACHE HIT
    ↓
Return cached response
    ↓
John Doe is NOT called
```

---

# 12. Next Problem: Stale Data

Assume John Doe initially has 10 trains.

At time 0:

```text
GET /trains
    ↓
CACHE MISS
    ↓
Call John Doe
    ↓
Get 10 trains
    ↓
Cache 10 trains
```

At the 10th second, add an 11th train to John Doe.

Now:

```text
John Doe Database = 11 trains
Client Cache      = 10 trains
```

Before cache expiry, another request gives:

```text
CACHE HIT
    ↓
Return 10 trains
```

This is called **stale data**.

---

# 13. Introducing TTL

TTL means:

```text
Time To Live
```

For this hands-on:

```text
TTL = 1 minute
```

The desired behavior is:

```text
Time 0
    ↓
Get 10 trains
    ↓
Cache 10 trains

Time 10 seconds
    ↓
Add 11th train to John Doe

Time 20 seconds
    ↓
CACHE HIT
    ↓
Still return cached 10 trains

After 1 minute
    ↓
Cache entry expires

Next request
    ↓
CACHE MISS
    ↓
Call John Doe
    ↓
Get latest 11 trains
```

---

# 14. Important TTL Behavior

After one minute, John Doe is not called automatically.

The actual behavior is:

```text
TTL expires
    ↓
Cached entry is no longer usable
    ↓
Next incoming request
    ↓
CACHE MISS
    ↓
Call John Doe
    ↓
Fetch fresh data
```

---

# 15. Add Caffeine

We now introduce Caffeine because we want cache expiration.

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

Keep the existing Spring cache dependency.

Conceptually:

```text
Spring Cache Abstraction
        +
Caffeine
        ↓
In-Memory Cache with TTL
```

---

# 16. Configure TTL

Add to `application.properties`:

```properties
spring.cache.type=caffeine
spring.cache.cache-names=allTrains
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=1m
```

The key setting is:

```properties
expireAfterWrite=1m
```

Meaning:

```text
Cache value written
    ↓
Valid for 1 minute
    ↓
Expires
```

The Java method does not need to change:

```java
@Cacheable("allTrains")
@Retry(name = "johnDoe")
public List<Train> findAllTrains() {
    System.out.println("Calling John Doe...");

    return restClient
            .get()
            .uri("/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {});
}
```

---

# 17. Complete Hands-On Experiment

## Initial State

```text
John Doe Database = 10 trains
Client Cache = empty
```

## Step 1: First Request

```text
GET /trains
    ↓
CACHE MISS
    ↓
Calling John Doe...
    ↓
Response = 10 trains
    ↓
Cache = 10 trains
TTL = 1 minute
```

## Step 2: Another Request

```text
GET /trains
    ↓
CACHE HIT
    ↓
Response = 10 trains
```

John Doe is not called.

## Step 3: Add New Data

At approximately 10 seconds, add an 11th train.

```text
John Doe Database = 11 trains
Client Cache = 10 trains
```

## Step 4: Request Before TTL Expiry

```text
GET /trains
    ↓
CACHE HIT
    ↓
Still return 10 trains
```

The data is stale, but the cache is still valid.

## Step 5: Wait for TTL

After one minute:

```text
Cache entry expires
```

## Step 6: Next Request

```text
GET /trains
    ↓
CACHE MISS
    ↓
Calling John Doe...
    ↓
John Doe returns 11 trains
    ↓
Cache = 11 trains
    ↓
TTL starts again
```

---

# 18. Complete Timeline

```text
0 sec
─────
GET /trains
CACHE MISS
John Doe returns 10
Cache stores 10

10 sec
──────
Add 11th train
John Doe DB = 11
Client Cache = 10

20 sec
──────
GET /trains
CACHE HIT
Return 10 stale trains

60+ sec
───────
Cache entry expired

Next Request
────────────
GET /trains
CACHE MISS
Call John Doe
Get 11 trains
Cache 11 trains
```

---

# 19. Cache HIT vs Cache MISS

| Situation | Cache Result | John Doe Called? |
|---|---|---|
| First request | MISS | Yes |
| Second request | HIT | No |
| Before TTL expiry | HIT | No |
| First request after TTL expiry | MISS | Yes |

---

# 20. Retry and Cache Together

Our method also uses:

```java
@Retry(name = "johnDoe")
```

Their responsibilities are different.

```text
Cache
─────
Avoid unnecessary HTTP calls.

Retry
─────
Handle temporary failures when an HTTP call is required.
```

Conceptually:

```text
Request
   ↓
Cache Check
   ├── HIT  → Return cached data
   │
   └── MISS
          ↓
        @Retry
          ↓
      Call John Doe
          ↓
      Store result in cache
```

On a cache hit, there is no HTTP call, so retry is unnecessary.

---

# 21. Final Architecture

```text
Client
   ↓
TrainController
   ↓
RailwayClient
   ↓
Spring Cache
   ├── HIT  → Return cached List<Train>
   │
   └── MISS
          ↓
        @Retry
          ↓
        RestClient
          ↓
    John Doe Railway
          ↓
      List<Train>
          ↓
    Caffeine Cache
          ↓
      Return response
```

---

# 22. Final Summary

```text
Without Cache
Every request calls John Doe.

With @Cacheable
First request calls John Doe.
Later requests use cached data.

Problem
Source data can change while the cache still contains old data.

TTL Solution
Cache remains valid for a configured duration.
After expiry, the next request fetches fresh data.
```

## Final Scenario

```text
John Doe initially = 10 trains
        ↓
First request caches 10
        ↓
11th train added at 10 seconds
        ↓
Client still returns cached 10
        ↓
TTL expires after 1 minute
        ↓
Next request calls John Doe
        ↓
Client receives and caches 11 trains
```

This hands-on introduces:

```text
Spring Cache
→ Cache HIT
→ Cache MISS
→ Stale Data
→ TTL
→ Caffeine
```
