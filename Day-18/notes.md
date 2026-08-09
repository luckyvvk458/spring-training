# Train Service Client – John Doe Railway Integration Notes

## 1. Project Context

We started with an existing Spring Boot application called **Train Service**.

Originally:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

The requirement is to replace direct Repository/database access with calls to the **John Doe Railway REST APIs**.

The new application is a separate Spring Boot application running on:

```text
http://localhost:8081
```

The existing John Doe Railway application runs on:

```text
http://localhost:8080
```

New architecture:

```text
Train Service Client :8081
        ↓
Controller
        ↓
Service
        ↓
RailwayClient
        ↓ HTTP REST
John Doe Railway :8080
        ↓
Repository
        ↓
Database
```

---

## 2. Why a Separate Client Application?

The new application acts as a **consumer/client of John Doe Railway**.

John Doe owns the railway data and exposes REST APIs.

The new application consumes those APIs and can implement additional application-specific business behavior.

This gives us a service-to-service architecture:

```text
John Doe Railway
    = source of truth for railway data

Train Service Client
    = consumer/application layer
```

---

# 3. Layer Responsibilities

## Controller

The Controller exposes APIs to clients of our new application.

```java
@RestController
@RequestMapping("/trains")
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping
    public List<Train> getAllTrains() {
        return trainService.findAllTrains();
    }
}
```

The Controller mainly deals with:

- HTTP requests
- URL mappings
- request parameters
- HTTP responses

It should not know how John Doe is called.

---

## Service

The Service layer owns application/business behavior.

```java
@Service
public class TrainService {

    private final RailwayClient railwayClient;

    public TrainService(RailwayClient railwayClient) {
        this.railwayClient = railwayClient;
    }

    public List<Train> findAllTrains() {
        return railwayClient.findAllTrains();
    }
}
```

Initially this may look like simple delegation. That is fine.

As requirements grow, the Service becomes the place for:

- effective departure calculation
- 30-minute exclusion
- 12-hour search window
- combining conditions
- ranking results
- business-specific filtering
- response transformation

---

## RailwayClient

`RailwayClient` owns communication with John Doe.

Its responsibility is:

> How do I communicate with the external railway service?

It contains:

- URL construction
- HTTP methods
- query parameters
- authentication headers
- REST invocation
- response deserialization

Example:

```java
@Component
public class RailwayClient {

    private final RestClient restClient;

    public RailwayClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8080")
                .build();
    }
}
```

---

# 4. Why Constructor Injection?

We used:

```java
public RailwayClient(RestClient.Builder builder) {
    this.restClient = builder
            .baseUrl("http://localhost:8080")
            .build();
}
```

instead of field injection such as:

```java
@Autowired
private RestClient.Builder builder;
```

Constructor injection is preferred because:

1. Dependencies are explicit.
2. Required dependencies cannot accidentally be omitted.
3. Dependencies can be `final`.
4. The class is easier to test.
5. The dependency graph is clear.
6. The object does not depend on field injection to become valid.

The same approach is used for:

```java
public TrainService(RailwayClient railwayClient)
```

and:

```java
public TrainController(TrainService trainService)
```

Dependency flow:

```text
Controller
    needs TrainService

TrainService
    needs RailwayClient

RailwayClient
    needs RestClient
```

---

# 5. RestClient

We use Spring's `RestClient` for synchronous REST calls.

Basic structure:

```java
restClient.get()
        .uri("/trains/getAll")
        .retrieve()
        .body(...);
```

The major pieces are:

```text
restClient
    ↓
get()
    ↓
uri(...)
    ↓
headers(...)
    ↓
retrieve()
    ↓
body(...)
```

---

# 6. Understanding findAllTrains()

The working method is:

```java
public List<Train> findAllTrains() {

    return restClient.get()
            .uri("/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {
            });
}
```

## `restClient.get()`

Specifies an HTTP GET request.

Equivalent HTTP:

```http
GET /trains/getAll
```

## `.uri("/trains/getAll")`

Specifies the endpoint on John Doe.

Because the RestClient has:

```java
.baseUrl("http://localhost:8080")
```

the complete URL becomes:

```text
http://localhost:8080/trains/getAll
```

## `.headers(...)`

Adds Basic Authentication:

```java
.headers(headers ->
        headers.setBasicAuth("admin", "admin123"))
```

## `.retrieve()`

Executes the request and prepares the response.

HTTP errors can become exceptions, such as:

```text
401 → Unauthorized
400 → Bad Request
404 → Not Found
500 → Server Error
```

## `.body(...)`

Specifies the Java type expected from the JSON response.

Because the response is:

```java
List<Train>
```

we use:

```java
new ParameterizedTypeReference<List<Train>>() {}
```

---

# 7. Why ParameterizedTypeReference?

John Doe returns a JSON array:

```json
[
  {
    "id": 1,
    "trainName": "Chennai Express"
  },
  {
    "id": 2,
    "trainName": "Bangalore Express"
  }
]
```

We want:

```java
List<Train>
```

The generic type information is therefore preserved using:

```java
new ParameterizedTypeReference<List<Train>>() {}
```

---

# 8. Basic Authentication

Current communication:

```text
Train Service Client :8081
        |
        | Authorization: Basic ...
        ↓
John Doe Railway :8080
```

The new application supplies credentials when calling John Doe.

For this exercise, Basic Authentication is enough to demonstrate service-to-service REST communication.

---

# 9. First Endpoint – Get All Trains

New application endpoint:

```http
GET http://localhost:8081/trains
```

Flow:

```text
Client
  ↓
TrainController
  ↓
TrainService.findAllTrains()
  ↓
RailwayClient.findAllTrains()
  ↓
GET http://localhost:8080/trains/getAll
  ↓
John Doe
  ↓
List<Train>
  ↓
Train Service Client
  ↓
Response
```

John Doe returns complete train objects containing nested coaches.

Example:

```json
{
  "id": 1,
  "trainName": "Chennai Express",
  "source": "Hyderabad",
  "destination": "Chennai",
  "coaches": [
    {
      "id": 1,
      "coachType": "SLEEPER",
      "availableSeats": 118,
      "price": 680
    },
    {
      "id": 2,
      "coachType": "AC",
      "availableSeats": 42,
      "price": 1580
    }
  ],
  "departureTime": "2026-08-06T08:30:00",
  "delayMinutes": 0
}
```

---

# 10. Entity Serialization Issue

Initially, directly returning bidirectional JPA entities caused recursive JSON:

```text
Train
  ↓
Coach
  ↓
Train
  ↓
Coach
  ↓
Train
  ↓
...
```

This happened because:

```text
Train → coaches
Coach → train
```

forms a bidirectional relationship.

Jackson attempted to serialize both directions.

The final response was changed so the back-reference was not serialized.

Correct shape:

```json
{
  "id": 1,
  "trainName": "Chennai Express",
  "source": "Hyderabad",
  "destination": "Chennai",
  "coaches": [
    {
      "id": 1,
      "coachType": "SLEEPER",
      "availableSeats": 118,
      "price": 680
    },
    {
      "id": 2,
      "coachType": "AC",
      "availableSeats": 42,
      "price": 1580
    }
  ],
  "departureTime": "2026-08-06T08:30:00",
  "delayMinutes": 0
}
```

---

# 11. Second Endpoint – Pricing

John Doe already supports a pricing/filtering operation.

For example:

```http
GET /trains/pricing?coachType=AC
```

John Doe can perform the generic filtering/sorting it supports.

Therefore, the new application should not unnecessarily do:

```text
get all trains
    ↓
filter AC
    ↓
sort price
```

if John Doe already provides that capability.

Instead:

```text
Train Service Client
    ↓
RailwayClient
    ↓
GET /trains/pricing?coachType=AC
    ↓
John Doe
    ↓
filtered/sorted result
```

This reduces unnecessary data transfer and duplicate processing.

---

# 12. Source of Truth vs Business Logic

A key architectural distinction:

> John Doe is the source of truth for railway facts. The consuming application can interpret those facts according to its own business requirements.

John Doe owns facts such as:

```text
departureTime
delayMinutes
availableSeats
price
coachType
trainName
```

Suppose John Doe returns:

```text
departureTime = 09:10
delayMinutes  = 40
```

Our application may calculate:

```text
effectiveDeparture = 09:10 + 40 minutes
                   = 09:50
```

if the application's business requirement says effective departure includes delay.

So:

```text
John Doe
    ↓
provides facts
    ↓
Train Service Client
    ↓
interprets facts according to business rules
```

---

# 13. When Should Business Logic Be in the New Application?

Business logic belongs in the new application when it is an application-specific rule that John Doe does not provide directly.

Examples:

## Effective departure

```text
effectiveDeparture =
    departureTime + delayMinutes
```

## 30-minute rule

> Ignore trains whose effective departure is within the next 30 minutes.

## 12-hour window

> Include trains whose effective departure falls within the allowed 12-hour window.

These rules can be implemented in `TrainService` if John Doe does not expose an API for that exact business operation.

---

# 14. Do Not Always Fetch Everything

Another important point:

It is incorrect to assume:

> The client application must always fetch every train and perform every filter locally.

If John Doe supports generic filters, use them.

For example:

```text
source=Hyderabad
coachType=AC
```

can reduce the amount of data returned.

Then the new application can apply additional business-specific rules to the smaller result set.

Good pattern:

```text
John Doe
    ↓
generic filtering / data reduction
    ↓
Train Service Client
    ↓
business-specific processing
```

---

# 15. Different Endpoints Can Have Different Response Shapes

This became important with the pricing endpoint.

`/trains/getAll` returns a train structure:

```json
{
  "id": 9,
  "trainName": "Jaipur Express",
  "source": "Hyderabad",
  "destination": "Jaipur",
  "coaches": [
    {
      "coachType": "AC",
      "availableSeats": 48,
      "price": 1180
    }
  ],
  "departureTime": "2026-08-06T20:50:00",
  "delayMinutes": 20
}
```

This maps naturally to:

```java
Train
```

But `/trains/pricing` returns a flatter pricing-oriented response:

```json
{
  "trainNumber": "9",
  "trainName": "Jaipur Express",
  "source": "Hyderabad",
  "destination": "Hyderabad",
  "departureTime": "2026-08-06T20:50:00",
  "delayMinutes": 0,
  "coachType": "AC",
  "availableSeats": 48,
  "price": 1180.0
}
```

That is a different response contract.

---

# 16. Why `id` and `coaches` Became Null

Initially, the pricing response was incorrectly deserialized into:

```java
List<Train>
```

Result:

```json
{
  "id": null,
  "trainName": "Jaipur Express",
  "source": "Hyderabad",
  "destination": "Hyderabad",
  "coaches": null
}
```

Reason:

The JSON contains:

```text
trainNumber
coachType
availableSeats
price
```

but `Train` expects:

```text
id
coaches
```

Jackson cannot automatically infer:

```text
trainNumber → id
```

or:

```text
coachType + availableSeats + price
    → coaches[]
```

Therefore, the endpoint needs its own DTO.

---

# 17. TrainPricingResponse

A dedicated DTO can represent the pricing endpoint:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainPricingResponse {

    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private Integer delayMinutes;
    private CoachType coachType;
    private Integer availableSeats;
    private Double price;
}
```

Client:

```java
public List<TrainPricingResponse> getTrainPricing(
        CoachType coachType) {

    return restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/trains/pricing")
                    .queryParam("coachType", coachType)
                    .build())
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<
                    List<TrainPricingResponse>>() {});
}
```

Now the JSON maps naturally:

```text
trainNumber    → trainNumber
trainName      → trainName
coachType      → coachType
availableSeats → availableSeats
price          → price
```

---

# 18. DTO Principle

Do not assume:

```text
One external service = one DTO
```

Instead:

```text
/getAll
    → Train

/pricing
    → TrainPricingResponse

/other endpoint
    → OtherResponseDTO
```

Different external endpoints can have different contracts.

The DTO should match the response contract actually returned by the endpoint.

---

# 19. Current Client Structure

A clean structure is:

```text
src/main/java
└── com.training.demo_train_service_client
    ├── DemoTrainServiceClientApplication.java
    │
    ├── controller
    │   └── TrainController.java
    │
    ├── service
    │   └── TrainService.java
    │
    ├── client
    │   └── RailwayClient.java
    │
    └── dto
        └── TrainPricingResponse.java
```

Domain classes can be kept in a model/domain package if desired:

```text
model/
    Train.java
    Coach.java
    CoachType.java
```

---

# 20. Current Architecture

```text
                 New Application :8081
                 ─────────────────────

                      Controller
                          │
                          ▼
                     TrainService
                          │
                          ▼
                    RailwayClient
                          │
                          │ HTTP
                          │ Basic Auth
                          ▼

                 John Doe Railway :8080
                 ─────────────────────
                          │
                      Controller
                          │
                          ▼
                       Service
                          │
                          ▼
                     Repository
                          │
                          ▼
                       MySQL
```

---

# 21. Completed REST Integrations

## Integration 1 – Get All Trains

New application:

```http
GET http://localhost:8081/trains
```

Client:

```java
railwayClient.findAllTrains();
```

John Doe:

```http
GET /trains/getAll
```

Response:

```java
List<Train>
```

---

## Integration 2 – Pricing

New application:

```http
GET http://localhost:8081/trains/pricing?coachType=AC
```

Client:

```java
railwayClient.getTrainPricing(coachType);
```

John Doe:

```http
GET /trains/pricing?coachType=AC
```

Response:

```java
List<TrainPricingResponse>
```

John Doe handles the generic filtering/sorting it already supports.

---

# 22. Repository Replacement

Old application:

```text
TrainService
    ↓
TrainRepository
    ↓
Database
```

New application:

```text
TrainService
    ↓
RailwayClient
    ↓
John Doe REST API
```

The Service layer remains.

The data-access mechanism changes from:

```text
Repository
```

to:

```text
REST client
```

This is the main migration objective.

---

# 23. Practical Decision Framework

For every new requirement, ask:

### Question 1

Does John Doe already provide the required operation?

If yes:

```text
Use John Doe's API.
```

Example:

```text
Get AC trains sorted by price
```

if John Doe already supports it.

---

### Question 2

Does John Doe provide the raw data needed to implement the rule?

If yes:

```text
Retrieve the required data
and implement the business rule in TrainService.
```

Example:

```text
effectiveDeparture =
    departureTime + delayMinutes
```

---

### Question 3

Can John Doe reduce the dataset using generic filters?

If yes:

```text
Use those filters first.
```

For example:

```text
source=Hyderabad
coachType=AC
```

Then apply application-specific processing to the smaller result set.

---

# 24. Example – Effective Departure

Suppose:

```text
departureTime = 09:10
delayMinutes = 40
```

Application:

```java
LocalDateTime effectiveDeparture =
        train.getDepartureTime()
             .plusMinutes(train.getDelayMinutes());
```

Result:

```text
09:50
```

This is an application-level business calculation.

---

# 25. Example – 30-Minute Rule

Suppose:

```text
currentTime = 09:00
```

Business requirement:

> Ignore trains whose effective departure is within the next 30 minutes.

Then:

```text
allowedStart = 09:30
```

If:

```text
effectiveDeparture = 09:20
```

exclude.

If:

```text
effectiveDeparture = 09:45
```

include.

---

# 26. Example – 12-Hour Window

Suppose:

```text
currentTime = 09:00
```

Then:

```text
startTime = 09:30
endTime   = 21:00
```

A train is included when:

```text
09:30 <= effectiveDeparture <= 21:00
```

John Doe provides:

```text
departureTime
delayMinutes
```

The application calculates:

```text
effectiveDeparture
allowed window
```

---

# 27. Why Not Implement Every Endpoint Together?

The remaining endpoints can be excellent practice:

- find by coach type
- sort by available seats
- sort by departure
- get coach information
- other supported John Doe queries
- combined queries

There is no need to implement every endpoint as guided work if the endpoint introduces no new concept.

The two completed integrations already demonstrate:

1. REST client creation
2. Basic Authentication
3. `RestClient`
4. GET requests
5. URI construction
6. query parameters
7. generic collection deserialization
8. service-to-service communication
9. DTO mapping
10. external API response contracts
11. source-of-truth responsibilities
12. business-logic ownership
13. using capabilities already provided by the external service

---

# 28. Potential Next Valuable Concept – External API Error Handling

A useful next step is handling failures from John Doe.

Examples:

```text
401 → Unauthorized
400 → Bad Request
404 → Not Found
500 → John Doe Server Error
timeout → John Doe unavailable
```

Currently `RestClient` can throw exceptions such as:

```text
HttpClientErrorException
```

A production-style application should translate external failures into appropriate application-level errors.

Potential flow:

```text
RailwayClient
     ↓
External API failure
     ↓
translate/handle exception
     ↓
TrainService / application exception
     ↓
GlobalExceptionHandler
     ↓
consistent ErrorResponse
```

This also connects naturally with the Global Exception Handling work completed in the original application.

---

# 29. Current Status

Completed:

- [x] New Spring Boot client application
- [x] Application running on port `8081`
- [x] `RestClient` configured
- [x] `RailwayClient` created
- [x] Basic Authentication configured
- [x] `findAllTrains()` integration
- [x] Controller → Service → Client flow
- [x] Correct handling of nested train/coach JSON
- [x] Pricing endpoint integration
- [x] Query parameter handling
- [x] Dedicated pricing response DTO
- [x] Understanding of external API response contracts
- [x] Understanding of source-of-truth vs application business logic
- [x] Understanding of when to use John Doe's capabilities vs local business rules

Practice candidates:

- [ ] coach-type filtering
- [ ] seat sorting
- [ ] departure sorting
- [ ] coach information
- [ ] combined queries
- [ ] effective departure
- [ ] 30-minute exclusion
- [ ] 12-hour window
- [ ] combined business rules
- [ ] external-service error handling

---

# 30. Key Takeaways

### Takeaway 1

`RailwayClient` is responsible for communication with John Doe.

### Takeaway 2

`TrainService` is responsible for application/business behavior.

### Takeaway 3

`Controller` exposes our application's APIs.

### Takeaway 4

Use John Doe's existing generic capabilities instead of unnecessarily reproducing them.

### Takeaway 5

Business-specific rules that John Doe does not provide belong in the new application.

### Takeaway 6

Do not force different external API response shapes into the same DTO.

### Takeaway 7

A dedicated DTO is justified when an endpoint's response contract differs from the domain model.

### Takeaway 8

The migration is not merely:

```text
Repository → REST
```

It establishes a service boundary:

```text
Train Service
      ↓
RailwayClient
      ↓
John Doe Railway
```

while keeping application-specific behavior in the appropriate layer.

---

# 31. One-Line Mental Model

Keep this model in mind while continuing the project:

```text
John Doe = "What railway data/capabilities do I provide?"

Train Service = "What does my application need to do with that data?"
```

And:

```text
Controller
    ↓
Service
    ↓
RailwayClient
    ↓
John Doe REST API
```
