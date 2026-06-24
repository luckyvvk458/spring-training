# Hibernate & JPA Fundamentals - Notes

## Learning Roadmap

```text
1. Hibernate CRUD
2. JPA CRUD
3. Why JPA Exists
4. Entity Lifecycle States
5. Persistence Context
6. First Level Cache
7. Dirty Checking
8. Repository Layer Basics
```

---

# 1. Hibernate CRUD Operations

Hibernate provides the `Session` API to interact with the database.

## Hibernate Architecture

```text
Application
     |
 SessionFactory
     |
   Session
     |
 Database
```

### SessionFactory

* Heavyweight object
* Created once per application
* Thread-safe
* Responsible for creating Session objects

```java
SessionFactory sessionFactory =
        HibernateUtil.getSessionFactory();
```

---

## CREATE

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

Employee employee = new Employee();
employee.setId(101);
employee.setName("Vivek");
employee.setSalary(50000);

session.persist(employee);

tx.commit();
session.close();
```

### Purpose

Stores a new entity in the database.

---

## READ

```java
Session session = sessionFactory.openSession();

Employee employee =
        session.get(Employee.class, 101);

System.out.println(employee);

session.close();
```

### Purpose

Retrieves an entity using its primary key.

### Note

`session.get()` returns `null` when no record exists.

---

## UPDATE

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

Employee employee =
        session.get(Employee.class, 101);

employee.setSalary(70000);

tx.commit();
session.close();
```

### Purpose

Updates an existing entity.

### Observation

No explicit update statement is required because Hibernate tracks changes made to managed entities.

---

## DELETE

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

Employee employee =
        session.get(Employee.class, 101);

session.remove(employee);

tx.commit();
session.close();
```

### Purpose

Deletes an entity from the database.

---

## Fetch All Records

```java
List<Employee> employees =
        session.createQuery(
                "from Employee",
                Employee.class)
                .list();

employees.forEach(System.out::println);
```

### Note

The query uses HQL (Hibernate Query Language).

```sql
FROM Employee
```

This is not SQL.

Hibernate converts HQL into SQL internally.

---

# 2. JPA CRUD Operations

JPA provides a standard API for persistence.

Hibernate is one of the implementations of JPA.

---

## JPA Architecture

```text
Application
     |
EntityManagerFactory
     |
 EntityManager
     |
 Database
```

---

## Hibernate vs JPA

| Hibernate      | JPA                  |
| -------------- | -------------------- |
| SessionFactory | EntityManagerFactory |
| Session        | EntityManager        |
| Transaction    | EntityTransaction    |
| get()          | find()               |
| persist()      | persist()            |
| remove()       | remove()             |
| createQuery()  | createQuery()        |

---

## CREATE

```java
EntityManager em =
        emf.createEntityManager();

em.getTransaction().begin();

Employee employee = new Employee();
employee.setId(201);
employee.setName("Rahul");

em.persist(employee);

em.getTransaction().commit();

em.close();
```

---

## READ

```java
Employee employee =
        em.find(Employee.class, 201);
```

Equivalent Hibernate API:

```java
session.get(Employee.class, 201);
```

---

## UPDATE

```java
Employee employee =
        em.find(Employee.class, 201);

employee.setSalary(90000);

em.getTransaction().commit();
```

---

## DELETE

```java
Employee employee =
        em.find(Employee.class, 201);

em.remove(employee);
```

---

## Fetch All Records

```java
List<Employee> employees =
        em.createQuery(
                "from Employee",
                Employee.class)
                .getResultList();
```

---

# 3. Why JPA Exists

Hibernate is an ORM framework.

JPA is a specification.

```text
JPA -> Specification
Hibernate -> Implementation
```

JPA provides a standard way of working with persistence.

Applications written using JPA APIs can switch between different implementations with minimal code changes.

Examples of JPA implementations:

* Hibernate
* EclipseLink
* OpenJPA

---

## Java Analogy

```java
List<String> names =
        new ArrayList<>();
```

Applications depend on the interface (`List`) rather than the implementation (`ArrayList`).

Similarly:

```text
JPA -> Interface / Specification

Hibernate -> Implementation
```

---

# 4. Entity Lifecycle States

Every entity moves through different states during its lifetime.

```text
Transient
    |
persist()
    ↓
Persistent
    |
Session Closed
    ↓
Detached
    |
remove()
    ↓
Removed
```

---

## Transient State

```java
Employee employee = new Employee();
```

Characteristics:

* Object exists only in memory
* Not associated with Session
* Not stored in the database

---

## Persistent State

```java
session.persist(employee);
```

Characteristics:

* Managed by Hibernate
* Stored in Persistence Context
* Changes are tracked automatically

---

## Detached State

```java
session.close();
```

Characteristics:

* Entity still exists
* Session is closed
* Hibernate no longer tracks changes

---

## Removed State

```java
session.remove(employee);
```

Characteristics:

* Entity is marked for deletion
* Deleted when transaction commits

---

# 5. Persistence Context

Persistence Context is an in-memory storage area maintained by Hibernate/JPA.

It contains all managed entities associated with the current Session or EntityManager.

```text
Session
    |
Persistence Context
    |
Managed Entities
```

Example:

```java
Employee employee =
        session.get(Employee.class, 101);
```

After retrieval, the entity becomes managed and is stored inside the Persistence Context.

---

# 6. First Level Cache

Every Session contains a First Level Cache.

The Persistence Context acts as the First Level Cache.

Example:

```java
Employee e1 =
        session.get(Employee.class, 101);

Employee e2 =
        session.get(Employee.class, 101);
```

Only one SQL query is executed.

The second retrieval comes from the cache.

---

## SQL Logging

Enable SQL logging:

```properties
hibernate.show_sql=true
```

This helps visualize when Hibernate accesses the database and when it uses the cache.

---

# 7. Dirty Checking

Dirty Checking is Hibernate's mechanism for automatically detecting changes made to managed entities.

Example:

```java
Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class, 101);

employee.setSalary(100000);

tx.commit();
```

No explicit update statement is required.

Hibernate compares:

```text
Original State
       vs
Current State
```

and automatically generates an UPDATE statement.

---

## Benefits

* Reduces boilerplate code
* Simplifies update operations
* Improves developer productivity

---

# 8. Repository Layer Basics

Direct database access from the Main class works for learning purposes, but it becomes difficult to maintain in larger applications.

The Repository Layer centralizes persistence logic.

---

## EmployeeRepository

```java
public class EmployeeRepository {
}
```

---

## findById()

```java
public Employee findById(int id) {

    try(Session session =
            HibernateUtil.getSessionFactory()
                    .openSession()) {

        return session.get(
                Employee.class,
                id);
    }
}
```

---

## findAll()

```java
public List<Employee> findAll() {

    try(Session session =
            HibernateUtil.getSessionFactory()
                    .openSession()) {

        return session.createQuery(
                "from Employee",
                Employee.class)
                .list();
    }
}
```

---

## save()

```java
public void save(Employee employee) {

    try(Session session =
            HibernateUtil.getSessionFactory()
                    .openSession()) {

        Transaction tx =
                session.beginTransaction();

        session.persist(employee);

        tx.commit();
    }
}
```

---

# Hibernate vs JPA CRUD Methods

| Operation | Hibernate                       | JPA                             |
| --------- | ------------------------------- | ------------------------------- |
| Create    | persist()                       | persist()                       |
| Read      | get()                           | find()                          |
| Update    | Managed Entity + Dirty Checking | Managed Entity + Dirty Checking |
| Delete    | remove()                        | remove()                        |
| Query     | createQuery()                   | createQuery()                   |

---

# Key Concepts Summary

```text
Hibernate
    ↓
Session
    ↓
Persistence Context
    ↓
First Level Cache
    ↓
Dirty Checking
```

```text
JPA
    ↓
Specification

Hibernate
    ↓
Implementation
```

```text
Entity Lifecycle

Transient
    ↓
Persistent
    ↓
Detached
    ↓
Removed
```
