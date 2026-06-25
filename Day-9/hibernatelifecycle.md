# Hibernate Entity Lifecycle & Persistence Context

## Overview

Every Hibernate entity goes through a series of lifecycle states during its lifetime. Understanding these states is essential because features such as the Persistence Context, Dirty Checking, First-Level Cache, and Automatic Flushing all depend on them.

---

## Table of Contents

1. Entity Lifecycle
2. Transient State
3. Persistent (Managed) State
4. Persistence Context
5. Detached State
6. Removed State
7. Java Heap vs Persistence Context
8. Dirty Checking
9. Why `update()` Is Not Needed for Managed Entities
10. Why Detached Entities Need `update()`
11. First-Level Cache
12. Flush
13. Summary
14. Interview Notes

---

# Entity Lifecycle

```text
                    new Employee()
                          │
                          ▼
                   +---------------+
                   |   Transient   |
                   +---------------+
                          │
            save() / persist()
            get() / find()
                          │
                          ▼
                   +---------------+
                   |  Persistent   |
                   |   (Managed)   |
                   +---------------+
                          │
      close() / clear() / evict() / detach()
                          │
                          ▼
                   +---------------+
                   |   Detached    |
                   +---------------+
                          │
             update() / merge()
                          │
                          ▼
                   +---------------+
                   |  Persistent   |
                   +---------------+
                          │
           delete() / remove()
                          │
                          ▼
                   +---------------+
                   |    Removed    |
                   +---------------+
```

---

# Transient State

A transient entity is a normal Java object created using the `new` keyword.

```java
Employee employee = new Employee();
employee.setName("John");
```

At this stage:

- The object exists only in the Java Heap.
- Hibernate is unaware of the object.
- The object is not present in the Persistence Context.
- No database record exists.
- Dirty Checking is not available.

| Property | Status |
|----------|--------|
| Java Object | Yes |
| Managed by Hibernate | No |
| Exists in Database | No |
| Dirty Checking | No |

---

# Persistent (Managed) State

An entity becomes persistent when it is associated with a Hibernate Session.

```java
Employee employee = new Employee();
session.save(employee);
```

or

```java
Employee employee = session.get(Employee.class, 1);
```

Once the entity becomes managed:

- Hibernate stores a reference to it inside the Persistence Context.
- Hibernate tracks every modification.
- Dirty Checking becomes active.
- Changes are synchronized during flush.

```text
Persistence Context

Employee#1
    │
    ▼
Employee Object (Java Heap)
```

| Property | Status |
|----------|--------|
| Java Object | Yes |
| Managed by Hibernate | Yes |
| Exists in Database | Yes |
| Dirty Checking | Yes |

---

# Persistence Context

The Persistence Context is an in-memory collection maintained by Hibernate.

It stores references to managed entities.

```text
Persistence Context

Employee#1  ─────────► Employee Object
Employee#2  ─────────► Employee Object
Employee#3  ─────────► Employee Object
```

The entity itself always remains in the Java Heap. Hibernate never creates another copy of the object.

---

# Detached State

A detached entity was once managed but is no longer associated with a Persistence Context.

```java
Employee employee = session.get(Employee.class, 1);

session.close();
```

The object still exists in memory, but Hibernate no longer tracks it.

```text
Java Heap

Employee Object

Persistence Context

(empty)
```

Changes made to a detached entity are not synchronized automatically.

```java
employee.setSalary(90000);
```

To make Hibernate manage it again:

```java
session.update(employee);

// or

entityManager.merge(employee);
```

| Property | Status |
|----------|--------|
| Java Object | Yes |
| Managed by Hibernate | No |
| Exists in Database | Yes |
| Dirty Checking | No |

---

# Removed State

A removed entity is scheduled for deletion.

```java
session.delete(employee);

// or

entityManager.remove(employee);
```

The actual SQL DELETE statement is executed during flush.

---

# Where Does Each State Exist?

| State | Java Heap | Persistence Context | Database |
|------|-----------|---------------------|----------|
| Transient | Yes | No | No |
| Persistent | Yes | Yes | Yes |
| Detached | Yes | No | Yes |
| Removed | Yes | Yes (until flush) | Deleted during flush |

---

# Java Heap vs Persistence Context

The entity never moves into the Persistence Context.

The object always stays in the Java Heap.

The Persistence Context only stores a reference.

Transient:

```text
Java Heap

Employee Object

Persistence Context

(empty)
```

Persistent:

```text
Java Heap

Employee Object
      ▲
      │
Persistence Context
      │
Employee#1
```

Detached:

```text
Java Heap

Employee Object

Persistence Context

(empty)
```

---

# Dirty Checking

Dirty Checking is Hibernate's mechanism for detecting changes to managed entities.

```java
Employee employee = session.get(Employee.class, 1);

employee.setSalary(90000);

transaction.commit();
```

There is no explicit `update()` call.

Hibernate compares:

Original Snapshot

```text
salary = 50000
```

Current State

```text
salary = 90000
```

Since the values differ, Hibernate generates:

```sql
UPDATE employee
SET salary = 90000
WHERE id = 1;
```

---

# Why `update()` Is Not Needed for Managed Entities

Flow:

```text
get()

↓

Managed Entity

↓

Modify Object

↓

commit()

↓

flush()

↓

Dirty Checking

↓

UPDATE SQL
```

Because the entity is already managed, Hibernate automatically tracks all changes.

---

# Why Detached Entities Need `update()`

```java
Session session1 = factory.openSession();

Employee employee = session1.get(Employee.class, 1);

session1.close();

employee.setSalary(90000);

Session session2 = factory.openSession();

session2.update(employee);
```

Flow:

```text
Detached Entity

↓

update()

↓

Managed Entity

↓

Flush

↓

UPDATE SQL
```

The purpose of `update()` is to reattach the detached entity to the Persistence Context.

---

# First-Level Cache

The Persistence Context is also known as the First-Level Cache.

```java
Employee e1 = session.get(Employee.class, 1);

Employee e2 = session.get(Employee.class, 1);
```

Only one SQL query is executed.

The second call returns the same managed object from the Persistence Context.

---

# Flush

Flush synchronizes the Persistence Context with the database.

Flush may occur:

- Before transaction commit
- Before executing certain queries
- When `flush()` is called explicitly

A flush does not commit the transaction.

Every commit performs a flush before committing.

---

# Summary

| State | Managed | Dirty Checking | Automatic Synchronization |
|------|----------|----------------|---------------------------|
| Transient | No | No | No |
| Persistent | Yes | Yes | Yes |
| Detached | No | No | No |
| Removed | Yes | Not Applicable | DELETE during flush |

---

# Interview Notes

- A transient entity is just a normal Java object.
- A persistent entity is managed by Hibernate.
- The Persistence Context stores references, not copies.
- Dirty Checking works only for managed entities.
- A detached entity exists in memory but is no longer tracked.
- `update()` reattaches a detached entity.
- `merge()` copies the detached entity's state into a managed entity.
- Flush synchronizes the Persistence Context with the database.
- Commit always performs a flush before committing.
