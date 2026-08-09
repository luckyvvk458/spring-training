# Resilience4j – Retry, Exponential Backoff & Circuit Breaker

## 1. Context

The Train Service Client consumes the John Doe Railway REST API.

```text
Client
   |
   v
Train Service Client :8081
   |
   v
Controller
   |
   v
Service
   |
   v
RailwayClient
   |
   | HTTP REST
   v
John Doe Railway :8080
   |
   v
Repository
   |
   v
Database
```

`RailwayClient` is the external-service integration boundary.

Because John Doe is outside our application, it can fail because of:

- Service being down
- Network problems
- Connection failures
- Connection timeouts
- Read timeouts
- Temporary service unavailability
- Server overload
- HTTP 5xx responses

This led us to resilience patterns.

---

# 2. Why Resilience Is Required

Without resilience:

```text
Train Service Client
        |
        v
John Doe
        |
       DOWN
        |
        v
Exception
        |
        v
Client gets error
```

A temporary downstream problem can unnecessarily become a user-visible failure.

For example, if John Doe is unavailable for two seconds and our request happens during that period, the user could receive an error even though John Doe recovers shortly afterward.

Retry helps with this type of temporary failure.

---

# 3. Retry

Retry means:

> If a downstream call fails with a retryable failure, try the same operation again.

Without Retry:

```text
Request
   |
   v
John Doe
   |
   X Failure
   |
   v
Exception
```

With Retry:

```text
Request
   |
   v
John Doe
   |
   X Failure
   |
   v
Wait
   |
   v
Retry
   |
   v
John Doe
```

Retry is useful when the failure is potentially temporary.

---

# 4. Resilience4j

We use Resilience4j to implement retry and circuit breaker behavior.

For Spring Boot 3.x:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.3.0</version>
</dependency>
```

We also need Spring AOP because the Resilience4j annotations are applied through Spring proxies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

# 5. Why Spring AOP Is Required

When we write:

```java
@Retry(name = "johnDoe")
public List<Train> findAllTrains() {
    ...
}
```

the annotation does not cause Java itself to execute the method multiple times.

Spring creates a proxy around the Spring bean.

Conceptually:

```text
Controller
    |
    v
Spring Proxy
    |
    | Retry logic
    |
    v
RailwayClient.findAllTrains()
    |
    v
RestClient
    |
    v
John Doe
```

When the method throws a configured exception, the Resilience4j interceptor can perform another attempt.

Without the AOP infrastructure, the method may execute only once.

---

# 6. Retry Configuration Name

We used:

```java
@Retry(name = "johnDoe")
```

The `johnDoe` value is not a keyword.

It is simply the name of the Resilience4j retry configuration.

It connects the Java annotation to the properties:

```properties
resilience4j.retry.instances.johnDoe.max-attempts=3
```

These names must match.

Correct:

```java
@Retry(name = "johnDoe")
```

```properties
resilience4j.retry.instances.johnDoe.max-attempts=3
```

Incorrect:

```java
@Retry(name = "johDoe")
```

with:

```properties
resilience4j.retry.instances.johnDoe.max-attempts=3
```

because `johDoe` and `johnDoe` are different configuration names.

---

# 7. Basic Retry Configuration

Initial configuration:

```properties
resilience4j.retry.instances.johnDoe.max-attempts=3
resilience4j.retry.instances.johnDoe.wait-duration=1s
```

Meaning:

```text
Attempt 1
   |
   X Failure
   |
wait 1 second
   |
Attempt 2
   |
   X Failure
   |
wait 1 second
   |
Attempt 3
   |
   X Failure
   |
Final failure
```

Important:

```text
max-attempts=3
```

means **3 total attempts**.

It does not mean:

```text
1 initial attempt + 3 retries
```

Therefore:

```text
1 initial attempt
+
2 additional attempts
=
3 total attempts
```

---

# 8. Testing Basic Retry

A simple test:

1. Start the Train Service Client.
2. Stop John Doe.
3. Call the client endpoint.
4. Observe the logs.

For example:

```http
GET http://localhost:8081/trains
```

The client calls:

```http
GET http://localhost:8080/trains/getAll
```

If John Doe is down, the request fails.

With retry enabled, the console should show:

```text
Calling John Doe...
Calling John Doe...
Calling John Doe...
```

This proves that the method was executed three times.

---

# 9. Exception We Observed

When John Doe was stopped, the client produced:

```text
org.springframework.web.client.ResourceAccessException
```

with a root cause such as:

```text
java.nio.channels.ClosedChannelException
```

We explicitly configured the Spring-level exception as retryable:

```properties
resilience4j.retry.instances.johnDoe.retry-exceptions=org.springframework.web.client.ResourceAccessException
```

Complete configuration:

```properties
resilience4j.retry.instances.johnDoe.max-attempts=3
resilience4j.retry.instances.johnDoe.wait-duration=1s
resilience4j.retry.instances.johnDoe.retry-exceptions=org.springframework.web.client.ResourceAccessException
```

---

# 10. RailwayClient Retry Method

```java
@Retry(name = "johnDoe")
public List<Train> findAllTrains() {

    System.out.println("Calling John Doe...");

    return restClient.get()
            .uri("/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {
            });
}
```

The responsibility is clear:

```text
RailwayClient
    |
    | external communication
    |
    +-- Retry protects the external call
```

---

# 11. Do Not Retry Every Failure

Retry should not blindly apply to every failure.

For example:

```text
400 Bad Request
401 Unauthorized
404 Not Found
```

usually should not be retried.

Repeating the exact same invalid request does not normally fix the problem.

Example:

```text
401 Unauthorized
   |
   v
Retry
   |
   v
401 Unauthorized
   |
   v
Retry
   |
   v
401 Unauthorized
```

This wastes time and adds unnecessary load.

Retry is more appropriate for transient failures such as:

```text
Connection failure
Connection timeout
Read timeout
Temporary network failure
503 Service Unavailable
```

The exact retry policy should be based on the application's requirements.

---

# 12. Fixed Retry Delay

Our first configuration used:

```properties
resilience4j.retry.instances.johnDoe.wait-duration=1s
```

This means every retry waits the same amount of time:

```text
Attempt 1
   |
   X
   |
1 second
   |
Attempt 2
   |
   X
   |
1 second
   |
Attempt 3
```

This is a fixed wait duration.

---

# 13. Exponential Backoff

Instead of waiting the same amount of time after every failure, we can progressively increase the delay.

This is called **exponential backoff**.

Configuration:

```properties
resilience4j.retry.instances.johnDoe.max-attempts=4
resilience4j.retry.instances.johnDoe.wait-duration=1s
resilience4j.retry.instances.johnDoe.enable-exponential-backoff=true
resilience4j.retry.instances.johnDoe.exponential-backoff-multiplier=2
resilience4j.retry.instances.johnDoe.retry-exceptions=org.springframework.web.client.ResourceAccessException
```

The retry pattern becomes approximately:

```text
Attempt 1
   |
   X Failure
   |
wait 1 second
   |
Attempt 2
   |
   X Failure
   |
wait 2 seconds
   |
Attempt 3
   |
   X Failure
   |
wait 4 seconds
   |
Attempt 4
   |
   X Failure
   |
Final failure
```

So:

```text
1s
2s
4s
8s
...
```

The general idea is:

```text
next delay = previous delay × multiplier
```

With:

```text
initial delay = 1 second
multiplier = 2
```

we get:

```text
1
2
4
8
```

---

# 14. Why Exponential Backoff Is Better

Suppose John Doe is overloaded.

Fixed retry:

```text
Request → fail
1s
Request → fail
1s
Request → fail
1s
Request → fail
```

The client keeps hitting an already unhealthy service.

Exponential backoff:

```text
Request → fail
1s
Request → fail
2s
Request → fail
4s
Request → fail
```

The downstream service receives progressively less pressure.

This gives it more time to recover.

---

# 15. Retry Test – Service Recovers

A particularly useful test is:

```text
John Doe initially DOWN
```

Then:

```text
Attempt 1 → FAIL
       |
       | 1s
       v
Attempt 2 → FAIL
       |
       | 2s
       v
Attempt 3 → SUCCESS
```

The original request from the client should succeed.

The user does not need to submit another request.

This demonstrates the real value of Retry:

> A transient downstream failure can be hidden from the caller if the downstream service recovers during the retry period.

---

# 16. Why Retry Alone Is Not Enough

Imagine John Doe is completely down for several minutes.

Without a Circuit Breaker:

```text
Request 1
    ↓
Retry
    ↓
Retry
    ↓
Retry
    ↓
Failure

Request 2
    ↓
Retry
    ↓
Retry
    ↓
Retry
    ↓
Failure

Request 3
    ↓
Retry
    ↓
Retry
    ↓
Retry
    ↓
Failure
```

If many users make requests, the application can continuously hammer an unhealthy John Doe service.

This is where Circuit Breaker becomes useful.

---

# 17. Circuit Breaker

Circuit Breaker means:

> If the downstream service is repeatedly failing, temporarily stop sending requests to it.

Instead of:

```text
Client
   ↓
Retry
   ↓
John Doe
   ↓
Failure
```

continuing indefinitely, we eventually do:

```text
Client
   ↓
Circuit Breaker OPEN
   ↓
Fail fast
```

John Doe is not even called while the circuit is open.

---

# 18. Circuit Breaker States

Circuit Breaker has three important states:

```text
CLOSED
OPEN
HALF_OPEN
```

## CLOSED

Normal state.

```text
Train Service
     |
     v
Circuit CLOSED
     |
     v
John Doe
```

Requests are allowed.

## OPEN

Too many failures have occurred.

```text
Train Service
     |
     v
Circuit OPEN
     |
     X
     |
John Doe
```

The external service is not called.

The application fails fast.

## HALF_OPEN

After the configured open-state wait period, the Circuit Breaker allows a limited test.

```text
OPEN
  |
  | wait
  v
HALF_OPEN
  |
  v
Test John Doe
```

If successful:

```text
HALF_OPEN
    |
 SUCCESS
    |
    v
 CLOSED
```

If it fails:

```text
HALF_OPEN
    |
  FAIL
    |
    v
 OPEN
```

---

# 19. Adding Circuit Breaker

Add the annotation to the RailwayClient method:

```java
@Retry(name = "johnDoe")
@CircuitBreaker(name = "johnDoe")
public List<Train> findAllTrains() {

    System.out.println("Calling John Doe...");

    return restClient.get()
            .uri("/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {
            });
}
```

Both annotations use:

```text
johnDoe
```

because they protect the same downstream service.

---

# 20. Circuit Breaker Configuration

Example:

```properties
resilience4j.circuitbreaker.instances.johnDoe.sliding-window-size=5
resilience4j.circuitbreaker.instances.johnDoe.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.johnDoe.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.johnDoe.wait-duration-in-open-state=10s
```

---

# 21. `sliding-window-size`

```properties
resilience4j.circuitbreaker.instances.johnDoe.sliding-window-size=5
```

The Circuit Breaker considers the most recent five calls.

Example:

```text
Call 1 → FAIL
Call 2 → FAIL
Call 3 → SUCCESS
Call 4 → FAIL
Call 5 → FAIL
```

Failures:

```text
4
```

Total calls:

```text
5
```

Failure rate:

```text
4 / 5 × 100 = 80%
```

---

# 22. `minimum-number-of-calls`

```properties
resilience4j.circuitbreaker.instances.johnDoe.minimum-number-of-calls=5
```

The Circuit Breaker does not evaluate the failure rate until at least five calls have been recorded.

This prevents the circuit from opening based on an extremely small sample.

---

# 23. `failure-rate-threshold`

```properties
resilience4j.circuitbreaker.instances.johnDoe.failure-rate-threshold=50
```

This means:

> Open the circuit when the failure rate reaches or exceeds 50%, after the minimum number of calls has been reached.

Example:

```text
5 calls
4 failures
1 success

Failure rate = 80%

80% >= 50%

Circuit → OPEN
```

---

# 24. `wait-duration-in-open-state`

```properties
resilience4j.circuitbreaker.instances.johnDoe.wait-duration-in-open-state=10s
```

Once the circuit opens, it remains open for approximately 10 seconds before transitioning toward HALF_OPEN.

During this period:

```text
Request
   ↓
Circuit OPEN
   ↓
Fail fast
```

John Doe is not called.

---

# 25. Retry + Circuit Breaker

We now have:

```text
@Retry
@CircuitBreaker
```

Conceptually:

```text
Client
   |
   v
Circuit Breaker
   |
   v
Retry
   |
   v
John Doe
```

Both are protecting the same external dependency.

The important distinction is:

```text
Retry:
"Try the downstream operation again."

Circuit Breaker:
"Stop calling the downstream operation when it is repeatedly unhealthy."
```

Because both are AOP-based resilience aspects, their interaction and ordering should be considered carefully in a production configuration.

---

# 26. Three Important Test Scenarios

## Scenario 1 – John Doe is healthy

```text
Client
   ↓
Circuit CLOSED
   ↓
John Doe
   ↓
SUCCESS
```

Expected:

```text
Successful call
```

## Scenario 2 – John Doe temporarily unavailable

```text
Client
   ↓
Retry
   ↓
John Doe DOWN
   ↓
FAIL
   ↓
wait
   ↓
Retry
   ↓
John Doe UP
   ↓
SUCCESS
```

Expected:

> Original client request eventually succeeds.

## Scenario 3 – John Doe continuously unavailable

```text
Client
   ↓
Retry
   ↓
Repeated failures
   ↓
Failure threshold reached
   ↓
Circuit OPEN
   ↓
Future requests fail fast
```

Expected:

> Eventually the application stops repeatedly calling John Doe.

---

# 27. Retry vs Circuit Breaker

| Feature | Retry | Circuit Breaker |
|---|---|---|
| Main purpose | Recover from transient failures | Protect against repeated failures |
| Behavior | Calls again | Stops calling |
| Waits between attempts | Yes | Open state has a wait period |
| Useful for temporary failure | Yes | Sometimes |
| Useful for continuously down service | Limited | Yes |
| Prevents repeated downstream load | No | Yes |
| Main idea | "Try again" | "Stop calling for now" |

Simple mental model:

```text
Retry:
"Maybe the next attempt will work."

Circuit Breaker:
"John Doe is clearly unhealthy.
Stop calling it for now."
```

---

# 28. Fallback Has Not Been Added Yet

Circuit Breaker decides:

```text
Should we call John Doe?
```

It does not automatically answer:

```text
What should our application return to its caller?
```

That is where fallback comes in.

Possible strategies:

### Option 1 – Return 503

```text
John Doe unavailable
        ↓
Train Service Client
        ↓
503 Service Unavailable
```

This is appropriate when current railway data is mandatory.

### Option 2 – Return cached data

```text
John Doe unavailable
        ↓
Cache
        ↓
Previously retrieved train data
```

This can be useful for some read operations, but train availability and pricing can become stale.

### Option 3 – Return a degraded response

Only if the business requirement explicitly allows it.

Fallback should therefore be designed according to business requirements rather than automatically returning stale data.

---

# 29. Combined Configuration

A combined example:

```properties
# Retry
resilience4j.retry.instances.johnDoe.max-attempts=4
resilience4j.retry.instances.johnDoe.wait-duration=1s
resilience4j.retry.instances.johnDoe.enable-exponential-backoff=true
resilience4j.retry.instances.johnDoe.exponential-backoff-multiplier=2
resilience4j.retry.instances.johnDoe.retry-exceptions=org.springframework.web.client.ResourceAccessException

# Circuit Breaker
resilience4j.circuitbreaker.instances.johnDoe.sliding-window-size=5
resilience4j.circuitbreaker.instances.johnDoe.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.johnDoe.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.johnDoe.wait-duration-in-open-state=10s
```

---

# 30. Current RailwayClient

```java
@Component
public class RailwayClient {

    private final RestClient restClient;

    public RailwayClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Retry(name = "johnDoe")
    @CircuitBreaker(name = "johnDoe")
    public List<Train> findAllTrains() {

        System.out.println("Calling John Doe...");

        return restClient.get()
                .uri("/trains/getAll")
                .headers(headers ->
                        headers.setBasicAuth("admin", "admin123"))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Train>>() {
                });
    }
}
```

---

# 31. Current Architecture

```text
                       Client
                         |
                         v
                Train Service :8081
                         |
                         v
                    Controller
                         |
                         v
                      Service
                         |
                         v
                  RailwayClient
                         |
              ┌──────────┴──────────┐
              |                     |
            Retry             Circuit Breaker
              |                     |
              └──────────┬──────────┘
                         |
                         v
                John Doe :8080
                         |
                         v
                     Database
```

Responsibilities:

```text
Retry
    ↓
Recover from temporary failures

Exponential Backoff
    ↓
Give the downstream service progressively more
time to recover

Circuit Breaker
    ↓
Stop repeatedly calling an unhealthy service

Fallback
    ↓
Decide what the application should return
when the downstream service remains unavailable
```

---

# 32. Current Project Learning Progress

Completed:

- [x] New Spring Boot client application
- [x] Application on port 8081
- [x] RestClient
- [x] Basic Authentication
- [x] John Doe REST integration
- [x] `findAllTrains()`
- [x] Pricing API integration
- [x] DTO mapping
- [x] Source-of-truth concept
- [x] Business logic ownership
- [x] Resilience4j
- [x] Spring AOP
- [x] Retry
- [x] Retry exception configuration
- [x] Fixed retry delay
- [x] Exponential backoff
- [x] Testing retry by stopping John Doe
- [x] Circuit Breaker
- [x] Circuit Breaker states
- [x] Circuit Breaker configuration
- [x] Retry vs Circuit Breaker

Possible next concept:

```text
Fallback / graceful degradation
```

---

# 33. Final Takeaway

The progression we have implemented is:

```text
Basic REST Client
        ↓
Retry
        ↓
Exponential Backoff
        ↓
Circuit Breaker
        ↓
Fallback
```

The mental model is:

```text
                    John Doe
                       |
                ┌──────┴──────┐
                |             |
             Healthy       Unhealthy
                |             |
                v             v
             SUCCESS        Retry
                              |
                         Still failing?
                              |
                              v
                       Circuit OPEN
                              |
                              v
                         Fail fast
                              |
                              v
                           Fallback
```

The key distinction is:

```text
Retry:
"Maybe the next attempt will work."

Circuit Breaker:
"John Doe is repeatedly unhealthy.
Stop calling it for now."

Fallback:
"John Doe is unavailable.
What should OUR application do?"
```
