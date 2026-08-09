# How Real-World Ticket Booking Systems Work
## Learning Through the John Doe Railway Application

---

# 1. Introduction

When we use different applications to book tickets, an interesting question comes to mind:

> **If multiple applications allow users to book tickets, how do all of them know whether a ticket or seat is available?**

For example, imagine that users can book John Doe Railway tickets through:

- John Doe Railway Website
- John Doe Railway Mobile App
- Partner Application A
- Partner Application B

The user interfaces may be different, but the railway inventory must remain correct.

This chapter uses the **John Doe Railway application** to understand how a real-world ticket booking system can work.

Our main goal is to learn how a backend system handles:

- Multiple applications
- Common ticket inventory
- Seat availability
- Booking requests
- Concurrent users
- Duplicate requests
- Temporary service failures

---

# 2. The Basic Idea

Multiple applications can provide a booking interface, but they should not independently make the final decision about ticket availability.

A simplified architecture is:

```text
              Multiple Applications
        ┌──────────┬──────────┬──────────┐
        │          │          │          │
        ▼          ▼          ▼          ▼
     Website    Mobile App  Partner A  Partner B
        │          │          │          │
        └──────────┴────┬─────┴──────────┘
                        │
                        ▼
              Common Booking APIs
                        │
                        ▼
                Central Inventory
                        │
                        ▼
             Final Booking Decision
```

The important idea is:

> **Many applications can show and sell tickets, but one authoritative system should make the final inventory and booking decision.**

---

# 3. Why Do We Need a Central Booking System?

Consider a train:

```text
Train Number: JD123
Train Name: John Doe Express
Route: Hyderabad → Pune
Available Seats: 1
```

Now three users use three different applications at almost the same time:

```text
User A → Partner Application A
User B → Partner Application B
User C → John Doe Railway Website
```

All three users may see:

```text
Available Seats = 1
```

What happens if every application maintains its own seat count?

```text
Partner A          → 1 seat available
Partner B          → 1 seat available
Official Website   → 1 seat available
```

All three applications may independently believe that they can sell the last seat.

This can lead to:

- Double booking
- Overselling
- Incorrect availability
- Customer complaints
- Difficult data correction

Therefore, the final decision should not be made independently by every application.

Instead:

```text
Partner Application A ────┐
Partner Application B ────┼────► Central Booking System
Official Website ─────────┘               │
                                          ▼
                                  Central Seat Inventory
```

This central inventory becomes the **source of truth**.

---

# 4. What Is a Source of Truth?

A **source of truth** is the system whose data is considered authoritative.

For example:

```text
Seat A10 = AVAILABLE
Seat A11 = AVAILABLE
Seat A12 = BOOKED
```

A partner application can display this information, but it should not permanently decide the state by itself.

Correct flow:

```text
Partner Application
        │
        │ "Can I book Seat A10?"
        ▼
Central Booking System
        │
        │ Checks current state
        ▼
Final Decision
```

The booking system may change the seat state like this:

```text
AVAILABLE → HELD → BOOKED
```

The central system controls these transitions.

---

# 5. Availability and Booking Are Different

This is one of the most important concepts in a ticket booking system.

## 5.1 Checking Availability

A client may call:

```http
GET /trains/JD123/availability
```

Example response:

```json
{
  "trainNumber": "JD123",
  "availableSeats": 1
}
```

This means:

> At this moment, the system reports one available seat.

However, this does **not** mean that the seat is reserved for the user.

---

## 5.2 Why Availability Can Become Stale

Consider this timeline:

```text
10:00:00.000 → User A checks availability → 1 seat
10:00:00.001 → User B checks availability → 1 seat

10:00:00.500 → User A books successfully
10:00:00.600 → User B tries to book
```

User B previously received:

```text
Available Seats = 1
```

But by the time User B sends the booking request, the seat may already be booked.

Therefore:

> **An availability response is a point-in-time read. It is not a reservation.**

---

# 6. Think About It

Suppose an e-commerce website shows:

```text
Only 1 item left
```

Two users see the message at the same time.

Can both users successfully purchase that item?

**Answer:** Not necessarily.

The same idea applies to train seats.

The system must make the final decision when the actual booking or purchase request happens.

---

# 7. A Typical Ticket Booking Flow

A simple booking flow is:

```text
1. Search Trains
        │
        ▼
2. Check Availability
        │
        ▼
3. Select Train / Seat
        │
        ▼
4. Temporarily Hold Seat
        │
        ▼
5. Complete Payment
        │
        ├── Payment Failed
        │       │
        │       ▼
        │   Release Seat
        │
        └── Payment Successful
                │
                ▼
6. Confirm Booking
                │
                ▼
7. Generate Ticket
```

---

# 8. Why Do We Need a Seat Hold?

Imagine this situation:

```text
User A selects Seat A10
        │
        ▼
User A goes to the payment page
        │
        ▼
User B also selects Seat A10
```

Without a temporary reservation, User B might book the seat while User A is still completing payment.

A common solution is to temporarily hold the seat.

Example:

```text
Seat Number: A10
Status: HELD
Hold Duration: 5 minutes
```

The seat state may change as follows:

```text
AVAILABLE
    │
    ▼
HELD
    │
    ├── Payment failed
    │        │
    │        ▼
    │    AVAILABLE
    │
    ├── Hold expired
    │        │
    │        ▼
    │    AVAILABLE
    │
    └── Payment successful
             │
             ▼
          BOOKED
```

This makes the booking process easier to manage.

---

# 9. The Last Seat Problem

Suppose only one seat remains:

```text
Available Seats = 1
```

At the same time:

```text
User A → Book last seat
User B → Book last seat
User C → Book last seat
```

The system should produce:

```text
User A → SUCCESS
User B → SOLD OUT
User C → SOLD OUT
```

Only one request should successfully claim the seat.

This is a **concurrency problem**.

---

# 10. What Is Concurrency?

Concurrency means that multiple operations happen during overlapping periods of time.

Example:

```text
Request A ───────┐
                 │
Request B ───────┼──► Booking System
                 │
Request C ───────┘
```

All requests may try to access the same resource.

In our example, the shared resource is:

```text
The last available seat
```

The backend system must coordinate these requests correctly.

---

# 11. Why Is a Simple Check Dangerous?

A naive approach might look like:

```text
Check available seats
        │
        ▼
If availableSeats > 0
        │
        ▼
Create booking
```

The problem is that multiple requests can read the same value before any request updates it.

Example:

```text
Initial availableSeats = 1

Request A reads → 1
Request B reads → 1
Request C reads → 1
```

All three requests may believe they can book the seat.

Therefore, the final inventory check and update must be coordinated.

---

# 12. Atomic Operations

An operation is called **atomic** when it is treated as one complete operation for the required consistency boundary.

Conceptually:

```text
IF seat is available
THEN reserve it
ELSE reject the request
```

The important idea is that the system should not allow multiple users to independently pass the same last-seat check.

A conceptual database update could be:

```sql
UPDATE train_inventory
SET available_seats = available_seats - 1
WHERE train_number = ?
  AND available_seats > 0;
```

The result tells us whether the reservation succeeded.

- One row updated → reservation succeeded.
- Zero rows updated → no seat was available.

There are different ways to implement concurrency control, such as:

- Atomic database updates
- Optimistic locking
- Pessimistic locking
- Version columns

These can be studied in more detail later.

---

# 13. Read Requests and Write Requests

Not all requests need the same handling.

## Read Request

Example:

```http
GET /trains/JD123/availability
```

This asks for information.

Many users may ask the same question:

```text
"What is the current availability of JD123?"
```

Possible optimizations include:

- Caching
- Request coalescing
- Rate limiting

---

## Write Request

Example:

```http
POST /bookings
```

This changes the system state.

A booking request requires stronger control because it can change inventory.

Important concerns include:

- Concurrency
- Atomicity
- Idempotency
- Transactions
- Validation

Remember:

> **Read operations are usually optimized for speed and scale. Write operations are protected for correctness.**

---

# 14. Request Coalescing

Now consider a high-traffic situation.

Suppose 10,000 users ask for the same information at nearly the same time:

```text
Train: JD123
Date: 2026-08-10
Coach: AC
```

Without coordination:

```text
10,000 requests
      │
      ▼
10,000 calls to downstream service
```

If every request directly calls the John Doe downstream service, the service may receive unnecessary duplicate work.

A possible optimization is **request coalescing**.

```text
10,000 similar requests
        │
        ▼
Request Coordinator
        │
        ├── First request starts downstream call
        │
        └── Other matching requests wait for result
                         │
                         ▼
                  One downstream response
                         │
            ┌────────────┼────────────┐
            ▼            ▼            ▼
         User 1       User 2       User N
```

The main idea is:

> If the same shareable information is already being fetched, other equivalent requests may reuse the result of the in-progress request.

---

# 15. Example Request Key

Equivalent requests can be identified using a request key.

For example:

```text
JD123:2026-08-10:AC
```

The flow may be:

```text
First Request
      │
      ▼
Is this request already in progress?
      │
   No │ Yes
      │    │
      ▼    ▼
Call      Join existing
service   in-progress request
      │    │
      └────┘
         │
         ▼
    Share result
```

In Java, a possible implementation may use:

```text
ConcurrentHashMap<RequestKey, CompletableFuture<Result>>
```

The exact implementation depends on the application requirements.

---

# 16. Request Coalescing vs Caching

These concepts are related but different.

## Request Coalescing

Focus:

> Do not perform the same downstream operation multiple times while it is already in progress.

Example:

```text
Request 1 → Starts the downstream call
Request 2 → Joins the in-progress call
Request 3 → Joins the in-progress call
```

---

## Caching

Focus:

> Reuse a result that has already been completed.

Example:

```text
Result stored for 10 seconds
        │
        ▼
Next request receives stored result
```

Comparison:

| Feature | Request Coalescing | Caching |
|---|---|---|
| Main purpose | Avoid duplicate in-progress calls | Reuse completed results |
| Works when | Same request is currently running | Previous result still exists |
| Freshness | Usually receives result from current call | May return an older result |
| Example | 10,000 simultaneous availability requests | Frequently requested train data |

For rapidly changing seat availability, cache freshness must be considered carefully.

---

# 17. Handling Temporary Failures

Suppose the John Doe downstream service is temporarily unavailable.

A simple retry approach may be:

```text
Attempt 1 → fail
Wait 1 second

Attempt 2 → fail
Wait 1 second

Attempt 3 → fail
Wait 1 second
```

Now imagine thousands of clients doing the same thing.

```text
Service is struggling
        │
        ▼
Thousands of clients retry every second
        │
        ▼
Additional load reaches the struggling service
```

This can make the problem worse.

---

# 18. Exponential Backoff

A better approach is to increase the delay between retries.

Example:

```text
Attempt 1 → Wait 1 second
Attempt 2 → Wait 2 seconds
Attempt 3 → Wait 4 seconds
Attempt 4 → Wait 8 seconds
```

This is called **exponential backoff**.

The basic idea is:

```text
Delay increases after each failed attempt.
```

This reduces the chance of repeatedly sending a large number of retries immediately.

---

# 19. What Is Jitter?

Suppose every client waits exactly 4 seconds.

Then all clients may retry again at exactly the same moment.

```text
Client 1 → retry at 4.0 seconds
Client 2 → retry at 4.0 seconds
Client 3 → retry at 4.0 seconds
```

This can create another sudden traffic spike.

**Jitter** adds a small random variation.

Example:

```text
Client 1 → retry at 3.7 seconds
Client 2 → retry at 4.2 seconds
Client 3 → retry at 4.5 seconds
```

This spreads retries over time.

---

# 20. Should Every Error Be Retried?

No.

Consider:

```text
Seat is already booked
```

Retrying may not help.

Consider:

```text
Invalid train number
```

Retrying also does not help.

Retries are generally useful for temporary problems such as:

- Temporary network failure
- Timeout
- Temporary downstream unavailability

A good retry policy usually has:

- Maximum retry attempts
- Maximum delay
- Timeout
- Backoff
- Jitter

---

# 21. Duplicate Booking Requests

Consider this situation:

```text
User clicks "Book Now"
```

Because of slow network or accidental double-clicking:

```text
POST /bookings
POST /bookings
```

The server may receive the same logical request more than once.

Without protection:

```text
Booking 1 created
Booking 2 created
```

This is a serious problem.

A solution is **idempotency**.

---

# 22. What Is Idempotency?

Idempotency means that repeating the same logical request should not produce duplicate side effects.

For example:

```http
POST /bookings
Idempotency-Key: booking-abc-123
```

The server can associate the request with the idempotency key.

If the same logical request arrives again:

```text
Same Idempotency-Key
        │
        ▼
Do not create another booking
        │
        ▼
Return existing result
```

This helps protect important operations such as:

- Ticket booking
- Payment
- Order creation

---

# 23. Important: Different Problems Need Different Solutions

Students should not confuse these concepts.

| Problem | Example | Main Idea |
|---|---|---|
| Duplicate reads | Many users check same train | Request coalescing |
| Frequently requested data | Same data requested repeatedly | Caching |
| Temporary failure | Downstream service timeout | Retry with backoff |
| Same request repeated | User clicks Book twice | Idempotency |
| Shared resource conflict | Many users book last seat | Concurrency control |

Each concept solves a different problem.

---

# 24. Complete John Doe Railway Flow

Let's connect everything together.

## Step 1: Search Trains

```http
GET /trains?source=Hyderabad&destination=Pune
```

Example response:

```json
[
  {
    "trainNumber": "JD123",
    "trainName": "John Doe Express",
    "source": "Hyderabad",
    "destination": "Pune"
  }
]
```

---

## Step 2: Check Availability

```http
GET /trains/JD123/availability
```

Example:

```json
{
  "trainNumber": "JD123",
  "availableSeats": 5
}
```

If many identical requests arrive together, request coalescing may reduce duplicate downstream calls.

---

## Step 3: Hold a Seat

```text
User selects Seat A10
        │
        ▼
Seat A10 changes:

AVAILABLE → HELD
```

Example response:

```json
{
  "holdId": "HOLD-1001",
  "seatNumber": "A10",
  "status": "HELD"
}
```

---

## Step 4: Payment

The user completes payment.

Possible outcomes:

```text
Payment Success → Continue booking
Payment Failure → Release hold
```

---

## Step 5: Confirm Booking

The system validates:

- Is the seat hold still valid?
- Has the hold expired?
- Is payment successful?
- Is this a duplicate request?
- Is the booking state valid?

Then:

```text
HELD → BOOKED
```

---

## Step 6: Generate Ticket

Example:

```json
{
  "bookingId": "BOOK-5001",
  "pnr": "JD987654",
  "status": "CONFIRMED"
}
```

---

# 25. Booking State Flow

A booking system can be easier to understand when states are explicitly defined.

```text
AVAILABLE
    │
    ▼
HELD
    │
    ├──────────────► EXPIRED
    │
    ├──────────────► RELEASED
    │
    ▼
PAYMENT_PENDING
    │
    ├──────────────► PAYMENT_FAILED
    │
    ▼
CONFIRMED
    │
    ▼
TICKET_ISSUED
```

The backend should validate valid state transitions.

For example, the system should not accidentally allow invalid transitions.

---

# 26. Common Failure Scenarios

## Scenario 1: Many Users Check the Same Availability

```text
10,000 users
      │
      ▼
Same availability request
```

Possible techniques:

- Request coalescing
- Caching, where appropriate
- Rate limiting

---

## Scenario 2: Many Users Book the Last Seat

```text
User A ─┐
User B ─┼──► One last seat
User C ─┘
```

Possible techniques:

- Atomic database updates
- Optimistic locking
- Pessimistic locking

---

## Scenario 3: Same Booking Request Is Sent Twice

```text
POST /bookings
POST /bookings
```

Possible technique:

```text
Idempotency key
```

---

## Scenario 4: Downstream Service Is Temporarily Failing

Possible techniques:

- Timeout
- Bounded retries
- Exponential backoff
- Jitter
- Circuit breaker

---

## Scenario 5: Payment Succeeds but Response Is Lost

```text
Booking Service
      │
      ▼
Payment Gateway
      │
      ▼
Payment SUCCESS
      │
      ▼
Response lost
```

The client may retry.

The backend must avoid:

- Charging twice
- Creating duplicate bookings

Possible mechanisms:

- Idempotency
- Payment transaction identifiers
- Persistent booking states
- Reconciliation

---

# 27. What to Learn First

Do not try to learn every distributed-system concept at once.

Learn in this order.

## Level 1: Basic Understanding

Understand:

> Multiple applications can provide booking interfaces, but inventory needs a controlled source of truth.

## Level 2: API Flow

Understand:

```text
Search
→ Availability
→ Hold
→ Payment
→ Confirm
→ Ticket
```

## Level 3: Concurrency

Understand:

> What happens when multiple users try to book the same last seat?

## Level 4: Reliability

Understand:

- Timeout
- Retry
- Exponential backoff
- Jitter

## Level 5: Duplicate Requests

Understand:

- Idempotency

## Level 6: Scaling Reads

Understand:

- Request coalescing
- Caching
- Rate limiting

---

# 28. Questions for Students

Think about the following questions.

### Question 1

If three applications show one available seat, who should make the final decision about whether the seat can be booked?

### Question 2

Why does this response not guarantee a booking?

```json
{
  "availableSeats": 1
}
```

### Question 3

What happens if three users try to book the last seat at exactly the same time?

### Question 4

What problem occurs when the same user clicks the Book button twice?

### Question 5

Why might retrying every failed request immediately make a system outage worse?

### Question 6

What is the difference between caching and request coalescing?

### Question 7

Why should a booking request be handled differently from an availability request?

---

# 29. Key Terms

## Source of Truth

The authoritative system whose data is considered correct.

## Availability

Information about whether inventory appears to be available at a particular time.

## Reservation / Hold

A temporary claim on inventory.

## Booking

A confirmed allocation of the resource.

## Concurrency

Multiple operations overlapping in time.

## Atomic Operation

An operation that is handled as one complete unit for the required consistency boundary.

## Request Coalescing

Avoiding duplicate in-progress work for equivalent requests.

## Cache

Stored data reused for later requests.

## Retry

Attempting an operation again after an appropriate failure.

## Exponential Backoff

Increasing the delay between retry attempts.

## Jitter

Random variation added to retry timing.

## Idempotency

Ensuring that repeating the same logical request does not create duplicate side effects.

---

# 30. Common Mistakes to Avoid

## Mistake 1: Treating Availability as Reservation

```text
Available = 1
```

does not mean the seat belongs to the user.

---

## Mistake 2: Allowing Every Application to Control Inventory

Different applications should not independently decide that the same last seat can be sold.

---

## Mistake 3: Retrying Every Error

Some errors are permanent business errors and retries will not help.

---

## Mistake 4: Treating Duplicate Booking Requests as New Requests

The same logical request may need idempotency protection.

---

## Mistake 5: Using Cached Data as the Final Booking Authority

Cached availability can become stale.

The final booking decision should use authoritative data and appropriate concurrency control.

---

# 31. Summary Table

| Requirement | Example | Possible Solution |
|---|---|---|
| Avoid duplicate in-progress reads | Many users check JD123 | Request coalescing |
| Reuse completed data | Same data requested repeatedly | Caching |
| Handle temporary failure | Downstream timeout | Retry with backoff |
| Prevent retry spikes | Many clients retry together | Jitter |
| Prevent duplicate booking | User submits twice | Idempotency |
| Prevent overselling | Many users want last seat | Atomic update / locking |
| Maintain correct inventory | Multiple applications sell tickets | Central source of truth |

---

# 32. What You Should Remember

1. **Multiple applications can use the same booking system.**
2. **A central system should control the final inventory decision.**
3. **Availability and booking are different operations.**
4. **Checking availability does not reserve a seat.**
5. **Multiple users can create concurrency problems.**
6. **The last-seat problem requires correct concurrency control.**
7. **Request coalescing and caching are different.**
8. **Retries should be controlled and should not overload a failing service.**
9. **Exponential backoff and jitter help control retries.**
10. **Idempotency prevents duplicate effects from repeated logical requests.**

---

# 33. The Most Important Takeaway

> **Many applications can show and sell the same ticket, but the final decision about inventory and booking must come from an authoritative central system.**

And from a backend engineering perspective:

> **Optimize read operations for performance and scale, but protect write operations for correctness and consistency.**

---

# 34. Connection to Future Topics

The John Doe Railway example can later be extended to study:

1. Database Transactions
2. Optimistic Locking
3. Pessimistic Locking
4. Spring Transaction Management
5. REST API Design
6. Caching
7. Request Coalescing
8. Idempotency
9. Retry Patterns
10. Circuit Breakers
11. Event-Driven Architecture
12. Kafka
13. Saga Pattern
14. Eventual Consistency
15. Microservices Design

These topics will become easier to understand when connected back to the same real-world booking example.

---

# 35. Interview Connection (Revision Section)

The main purpose of these notes is to understand the system as backend developers. The same concepts are also useful during interviews.

A concise explanation is:

> "In a ticket booking system, multiple applications can provide booking interfaces, but the inventory and final booking decision should come from a central authoritative system. Availability is only a point-in-time read and does not guarantee a booking. For high-volume read traffic, techniques such as caching or request coalescing can reduce duplicate work. For booking, the system must handle concurrency correctly so that only one request can claim the last available seat. Idempotency prevents duplicate booking operations, while bounded retries with exponential backoff and jitter help handle temporary downstream failures."

---

# Final Revision

```text
Multiple Applications
        │
        ▼
Common Booking APIs
        │
        ▼
Central Inventory
        │
        ├── Availability → Read / optimize carefully
        │
        └── Booking → Write / protect carefully
                    │
                    ▼
         Concurrency + Idempotency
                    │
                    ▼
              Correct Ticket Booking
```

**Remember:**

> **Availability tells us what is available now. Booking makes the final decision.**
