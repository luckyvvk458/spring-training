# Session 4 -- Spring Transaction Propagation & Self Invocation

## Detailed Student Notes

### Learning Outcomes

-   Understand transaction propagation.
-   Learn REQUIRED and REQUIRES_NEW.
-   Understand the Self Invocation problem.
-   Understand why @Transactional sometimes does not work.

------------------------------------------------------------------------

# 1. Existing Design

BookingController

↓

BookingService (@Transactional)

↓

BookingRepository

One transaction.

------------------------------------------------------------------------

# 2. New Requirement

Whenever a booking is created, create an audit record.

BookingService calls AuditService.

``` java
@Transactional
public void bookTicket() {

    bookingRepository.save(new Booking(...));

    auditService.saveAudit();
}
```

AuditService:

``` java
@Transactional
public void saveAudit() {

    auditRepository.save(new Audit("BOOKING CREATED"));
}
```

Question:

How many transactions?

Answer:

Only ONE.

AuditService joins the existing transaction.

This is Propagation.REQUIRED.

------------------------------------------------------------------------

# REQUIRED

Definition:

If a transaction already exists, join it. Otherwise create a new
transaction.

It is the default propagation.

Experiment:

Throw an exception after saveAudit().

Result:

Booking -\> Rollback

Audit -\> Rollback

Reason:

Both belong to one transaction.

------------------------------------------------------------------------

# REQUIRES_NEW

Business Requirement:

Even if booking fails, keep the audit.

Change AuditService.

``` java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAudit() {

    auditRepository.save(new Audit("BOOKING CREATED"));
}
```

Execution:

Transaction A

Booking

↓

Pause

↓

Transaction B

Audit

↓

Commit B

↓

Resume A

↓

Exception

↓

Rollback A

Final Result:

Booking -\> Rollback

Audit -\> Committed

------------------------------------------------------------------------

# REQUIRED vs REQUIRES_NEW

REQUIRED

-   Join existing transaction
-   Default behavior

REQUIRES_NEW

-   Suspend existing transaction
-   Always start a new transaction

------------------------------------------------------------------------

# Self Invocation

Example:

``` java
@Service
public class BookingService {

    @Transactional
    public void saveBooking() {

        bookingRepository.save(...);

    }

    public void createBooking() {

        saveBooking();

    }

}
```

Question:

Will transaction start?

Answer:

No.

Reason:

Spring AOP works through a proxy.

External Call:

Controller

↓

Proxy

↓

@Transactional Method

Works.

Internal Call:

createBooking()

↓

this.saveBooking()

Proxy is bypassed.

No transaction starts.

------------------------------------------------------------------------

# Solution

Recommended:

Move saveBooking() to another service.

BookingService

↓

AuditService

The call now passes through Spring Proxy.

------------------------------------------------------------------------

# Best Practices

-   Keep transactions in Service layer.
-   Use REQUIRED for a single business operation.
-   Use REQUIRES_NEW only for independent business work.
-   Avoid internal calls to @Transactional methods.

------------------------------------------------------------------------

# Hands-on Exercises

1.  Create BookingService and AuditService.
2.  Verify REQUIRED behavior.
3.  Change to REQUIRES_NEW.
4.  Observe database changes.
5.  Demonstrate Self Invocation.
6.  Move method to another service and verify.

------------------------------------------------------------------------

# Interview Questions

1.  What is propagation?
2.  What is REQUIRED?
3.  What is REQUIRES_NEW?
4.  What is Self Invocation?
5.  Why does @Transactional fail during internal calls?
6.  How do you fix Self Invocation?
