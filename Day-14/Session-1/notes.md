# Spring Transactions 
## Student Notes

> These notes are designed to explain **why transactions exist** before introducing **Spring's `@Transactional`**.

---

# Learning Objectives

By the end of this session, you should understand:

- Why transactions are required
- What problem they solve
- Database transactions (BEGIN, COMMIT, ROLLBACK)
- How Java controls database transactions using JDBC
- Why Spring introduced `@Transactional`
- Spring Transaction Manager
- Spring AOP & Proxy
- Self Invocation Problem
- Propagation (`REQUIRED` and `REQUIRES_NEW`)
- Common production use cases

---

# 1. Why Do We Need Transactions?

## ATM Example

Withdraw ₹5000.

Steps:

1. Deduct ₹5000 from account
2. Dispense ₹5000 cash

What if electricity goes off after deduction but before cash is dispensed?

Possible states:

```
✔ Deduct + Dispense
```

or

```
✔ Nothing happens
```

Never

```
✘ Deduct only
```

This introduces the need for a transaction.

---

## Train Booking Example

Booking involves:

```
Check Seat

↓

Reduce Seat

↓

Save Booking
```

Suppose

```
Seat Reduced ✔

Booking Save ❌
```

Database becomes

```
Seats = 49

Bookings = 0
```

Question:

Is this correct?

No.

We need:

```
Either

Everything succeeds

OR

Everything is undone.
```

---

# 2. What is a Transaction?

A **transaction** is a group of operations treated as one unit of work.

Either

```
All Success
```

OR

```
All Failure
```

Nothing in between.

---

# 3. Database Transactions

Databases support transactions using

```
BEGIN

↓

SQL 1

↓

SQL 2

↓

SQL 3

↓

COMMIT
```

If any SQL fails

```
BEGIN

↓

SQL 1

↓

SQL 2

↓

Exception

↓

ROLLBACK
```

## Commit

Makes all changes permanent.

## Rollback

Undoes every change made during the current transaction.

---

# 4. Auto Commit

By default, JDBC connections use:

```
autoCommit = true
```

Meaning every SQL statement commits immediately.

Example

```
UPDATE ...

COMMIT

INSERT ...

COMMIT
```

If INSERT fails,

UPDATE is already committed.

This leads to inconsistent data.

---

# 5. How Does Java Control Database Transactions?

Java never talks directly to MySQL.

```
Java Application

↓

JDBC Driver

↓

Database
```

The JDBC `Connection` exposes transaction APIs.

```java
Connection con = DriverManager.getConnection(...);

con.setAutoCommit(false);

con.commit();

con.rollback();
```

Important:

Java does **not** implement transactions.

It simply requests the database to:

- Begin
- Commit
- Rollback

---

# 6. Manual JDBC Transaction

```java
Connection con = DriverManager.getConnection(...);

con.setAutoCommit(false);

try {

    reduceSeat(con);

    saveBooking(con);

    con.commit();

} catch(Exception e){

    con.rollback();

} finally {

    con.close();

}
```

Problem:

Every service method requires the same boilerplate.

---

# 7. Why Spring?

Imagine writing the above code in:

- BookingService
- PaymentService
- EmployeeService
- OrderService
- TrainService

The transaction code is identical.

Spring automates it.

---

# 8. Introducing @Transactional

Instead of writing:

```java
con.setAutoCommit(false);

try {

    ...

    con.commit();

} catch(Exception e){

    con.rollback();

}
```

we simply write

```java
@Transactional
public void bookTicket() {

    reduceSeat();

    saveBooking();

    deductMoney();

}
```

Spring handles the boilerplate.

---

# 9. What Really Happens?

```
Client

↓

Spring Proxy

↓

Transaction Manager

↓

JDBC Connection

↓

Database
```

Execution Flow

```
Start Transaction

↓

Execute Method

↓

Success?

↓

Yes → Commit

No → Rollback
```

Conceptually Spring performs:

```java
begin();

try {

    originalMethod();

    commit();

} catch(Exception e){

    rollback();

    throw e;
}
```

---

# 10. Why Spring Uses AOP

Transactions are a cross-cutting concern.

Business logic:

```
Reduce Seat

Save Booking

Deduct Money
```

Infrastructure:

```
Open Connection

Begin Transaction

Commit

Rollback

Close Connection
```

Spring AOP wraps your method.

---

# 11. Self Invocation Problem

```java
@Service
public class BookingService {

    @Transactional
    public void bookTicket(){

        saveBooking();

    }

    @Transactional
    public void saveBooking(){

    }

}
```

Call Flow

```
Client

↓

Proxy

↓

bookTicket()

↓

saveBooking()
```

The call from `bookTicket()` to `saveBooking()` stays inside the same object.

It never goes through the proxy.

Result:

`@Transactional` on `saveBooking()` is ignored.

---

# 12. Transaction Propagation

Propagation answers one question:

> What should Spring do if a transaction already exists?

---

## REQUIRED (Default)

Decision Tree

```
Transaction Exists?

YES → Join Existing

NO → Create New
```

Suitable for one business operation.

Example

```
Reduce Seat

↓

Save Booking

↓

Deduct Money
```

One transaction.

One commit.

One rollback.

---

## REQUIRES_NEW

Decision Tree

```
Transaction Exists?

YES

↓

Suspend Existing

↓

Create New

↓

Commit

↓

Resume Old Transaction
```

Use when the operation must be independent.

Examples

- Audit Log
- Notification Log
- SMS History
- API Log
- Failed Login History

---

# 13. REQUIRED vs REQUIRES_NEW

| REQUIRED | REQUIRES_NEW |
|-----------|--------------|
| Joins existing transaction | Always creates a new transaction |
| Default propagation | Explicitly configured |
| One commit | Independent commit |
| One rollback | Independent rollback |

---

# 14. Self Invocation + REQUIRES_NEW

This does NOT work:

```java
@Transactional
public void bookTicket(){

    saveAudit();

}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAudit(){

}
```

Reason:

Internal method call skips the Spring proxy.

Correct design:

```
BookingService

↓

NotificationService

↓

Proxy

↓

REQUIRES_NEW works
```

---

# 15. Complete Architecture

```
Client

↓

Controller

↓

BookingService Proxy

↓

Transaction Manager

↓

JPA Repository

↓

JDBC

↓

Database
```

Remember:

- Database implements transactions.
- JDBC exposes transaction operations.
- Spring automates transaction management.
- `@Transactional` tells Spring where to apply it.

---

# Summary

1. Transactions prevent inconsistent data.
2. Databases provide BEGIN, COMMIT and ROLLBACK.
3. Java uses JDBC Connection APIs.
4. Spring removes boilerplate using `@Transactional`.
5. Spring uses AOP proxies.
6. Self-invocation skips the proxy.
7. REQUIRED joins an existing transaction.
8. REQUIRES_NEW creates an independent transaction.
9. Isolation is intentionally **not** covered in this session because it solves concurrent transaction problems, not single transaction consistency.
