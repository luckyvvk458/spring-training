# Understanding `@Transactional` from Scratch

## Student Notes

These notes explain transactions by building intuition first.

# Learning Objectives

-   Explain why transactions are required.
-   Understand the idea of **All or Nothing**.
-   Explain COMMIT and ROLLBACK.
-   Understand what Spring does when it sees `@Transactional`.
-   Relate `@Transactional` to Spring AOP.

------------------------------------------------------------------------

# The Business Problem

When a passenger books a ticket, two operations must happen.

``` text
Book Ticket
    |
    v
Save Booking
    |
    v
Save Audit Record
```

Business Rule:

> Every successful booking must also have an audit record.

------------------------------------------------------------------------

# Initial Service

``` java
public void bookTicket(Booking booking) {

    bookingRepository.save(booking);

    Audit audit = new Audit();
    audit.setAction("Booking Created");

    auditRepository.save(audit);
}
```

Everything works when there is no failure.

------------------------------------------------------------------------

# Simulating Failure

``` java
public void bookTicket(Booking booking) {

    bookingRepository.save(booking);

    simulateFailure();

    Audit audit = new Audit();
    audit.setAction("Booking Created");

    auditRepository.save(audit);
}

private void simulateFailure() {
    throw new RuntimeException("Audit service unavailable");
}
```

Database after execution:

Booking table contains the new booking.

Audit table does not contain the audit record.

This is called **partial work**.

------------------------------------------------------------------------

# The Business Requirement

The business does not ask for `@Transactional`.

It asks for:

``` text
Booking
+
Audit

↓

Either BOTH happen

OR

Neither happens.
```

This is the **All or Nothing** principle.

------------------------------------------------------------------------

# Understanding Transactions

Think of a transaction as temporary work.

``` text
START TRANSACTION

↓

Temporary Changes

↓

Success?

YES → COMMIT

NO → ROLLBACK
```

COMMIT means:

> Make every change permanent.

ROLLBACK means:

> Discard every temporary change.

Rollback is **not** the same as DELETE.

------------------------------------------------------------------------

# MySQL Demonstration

``` sql
START TRANSACTION;

INSERT INTO booking(passenger_name, train_name)
VALUES ('Rahul','Rajdhani');

SELECT * FROM booking;

ROLLBACK;

SELECT * FROM booking;
```

Repeat using COMMIT instead of ROLLBACK and observe the difference.

------------------------------------------------------------------------

# Spring Transaction Management

Without Spring:

``` text
Controller
   |
Service
   |
Repository
   |
Database
```

With Spring:

``` text
Controller
   |
Spring Proxy
   |
BookingService
   |
Repository
   |
Database
```

The proxy performs:

``` text
START TRANSACTION

↓

Execute Service Method

↓

Success?

YES → COMMIT

NO → ROLLBACK
```

------------------------------------------------------------------------

# Using @Transactional

``` java
@Transactional
public void bookTicket(Booking booking) {

    bookingRepository.save(booking);

    simulateFailure();

    Audit audit = new Audit();
    audit.setAction("Booking Created");

    auditRepository.save(audit);
}
```

If an exception occurs, Spring rolls back the entire transaction.

------------------------------------------------------------------------

# AOP Connection

Conceptually, Spring behaves like:

``` java
beginTransaction();

try {

    joinPoint.proceed();

    commit();

} catch(Exception ex) {

    rollback();

    throw ex;
}
```

This is why `@Transactional` is implemented using Spring AOP.

------------------------------------------------------------------------

# Summary

-   Transactions solve the partial work problem.
-   The goal is **All or Nothing**.
-   COMMIT makes changes permanent.
-   ROLLBACK discards temporary work.
-   Spring automates transaction management using AOP.
