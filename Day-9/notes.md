# Hibernate & JPA - Training Notes

## Learning Roadmap

```text
1. Hibernate Native CRUD
2. Why JPA?
3. Migrating from Hibernate APIs to JPA APIs
4. Entity Lifecycle States
5. Persistence Context
6. First-Level Cache
7. Dirty Checking
8. Repository Layer Basics
```

---

# 1. Hibernate Native CRUD

Hibernate provides its own native API to perform database operations.

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

session.save(employee);

tx.commit();
session.close();
```

### Method Used

```java
session.save(entity);
```

Saves a new entity into the database.

---

## READ

```java
Session session = sessionFactory.openSession();

Employee employee =
        session.get(Employee.class,101);

System.out.println(employee);

session.close();
```

### Method Used

```java
session.get(Employee.class,id);
```

Returns the entity if found, otherwise returns `null`.

---

## UPDATE

```java
Session session = sessionFactory.openSession();

Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class,101);

employee.setSalary(80000);

session.update(employee);

tx.commit();

session.close();
```

### Method Used

```java
session.update(entity);
```

Updates an existing entity.

---

## DELETE

```java
Session session = sessionFactory.openSession();

Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class,101);

session.delete(employee);

tx.commit();

session.close();
```

### Method Used

```java
session.delete(entity);
```

Deletes the entity from the database.

---

## Fetch All Employees

```java
List<Employee> employees =
        session.createQuery(
                "from Employee",
                Employee.class)
               .list();

employees.forEach(System.out::println);
```

### HQL

```sql
FROM Employee
```

HQL operates on Java entities rather than database tables.

Hibernate automatically converts HQL into SQL.

---

# 2. Why JPA?

So far, the application depends directly on Hibernate APIs.

```text
Application
      |
 Hibernate
      |
 Database
```

Examples:

```java
session.save()

session.update()

session.delete()

session.get()
```

These APIs belong specifically to Hibernate.

If an organization decides to switch to another ORM framework, application code must be modified.

To solve this problem, Java introduced JPA.

---

## What is JPA?

JPA stands for **Java Persistence API**.

It is a specification that defines a standard way to perform persistence operations.

Hibernate is one implementation of the JPA specification.

```text
Application
      |
      JPA
      |
--------------------------------
|             |                |
Hibernate   EclipseLink    OpenJPA
```

Applications depend on JPA rather than a specific ORM implementation.

---

## Java Analogy

```java
List<String> names =
        new ArrayList<>();
```

Applications depend on the interface (`List`) instead of the implementation (`ArrayList`).

Similarly,

```text
JPA
↓
Specification

Hibernate
↓
Implementation
```

---

# 3. Migrating from Hibernate APIs to JPA APIs

Only the persistence API changes.

The entity classes remain exactly the same.

---

## Hibernate

```java
Session session =
        sessionFactory.openSession();
```

## JPA

```java
EntityManager em =
        entityManagerFactory.createEntityManager();
```

---

## CREATE

### Hibernate

```java
session.save(employee);
```

### JPA

```java
em.persist(employee);
```

---

## READ

### Hibernate

```java
session.get(Employee.class,101);
```

### JPA

```java
em.find(Employee.class,101);
```

---

## UPDATE

### Hibernate

```java
session.update(employee);
```

### JPA

```java
em.merge(employee);
```

Later, it will be shown that `merge()` is not required when the entity is already managed by the Persistence Context.

---

## DELETE

### Hibernate

```java
session.delete(employee);
```

### JPA

```java
em.remove(employee);
```

---

## QUERY

### Hibernate

```java
session.createQuery(...)
```

### JPA

```java
em.createQuery(...)
```

---

## API Comparison

| Operation | Hibernate     | JPA           |
| --------- | ------------- | ------------- |
| Create    | save()        | persist()     |
| Read      | get()         | find()        |
| Update    | update()      | merge()       |
| Delete    | delete()      | remove()      |
| Query     | createQuery() | createQuery() |

---

# 4. Entity Lifecycle States

Every entity passes through different lifecycle states.

```text
Transient
      |
save()/persist()
      ↓
Persistent
      |
Session Closed
      ↓
Detached
      |
delete()/remove()
      ↓
Removed
```

---

## Transient

```java
Employee employee = new Employee();
```

Characteristics

* Exists only in memory
* Not associated with Session
* Not stored in the database

---

## Persistent

```java
session.save(employee);
```

or

```java
em.persist(employee);
```

Characteristics

* Managed by Hibernate
* Stored inside Persistence Context
* Hibernate monitors all changes

---

## Detached

```java
session.close();
```

Characteristics

* Entity still exists
* No longer managed
* Changes are not synchronized automatically

---

## Removed

```java
session.delete(employee);
```

or

```java
em.remove(employee);
```

Entity is scheduled for deletion.

---

# 5. Persistence Context

Persistence Context is an in-memory storage area that contains all managed entities.

Every Session (Hibernate) or EntityManager (JPA) owns one Persistence Context.

```text
Session / EntityManager
           |
Persistence Context
           |
 Managed Entities
```

Example

```java
Employee employee =
        session.get(Employee.class,101);
```

After retrieval, the entity becomes managed.

---

# 6. First-Level Cache

Every Persistence Context maintains a First-Level Cache.

Example

```java
Employee e1 =
        session.get(Employee.class,101);

Employee e2 =
        session.get(Employee.class,101);
```

Only one SQL query is executed.

The second object is returned directly from the cache.

---

## Enable SQL Logging

```properties
hibernate.show_sql=true
```

This allows observation of the generated SQL statements.

---

# 7. Dirty Checking

Dirty Checking automatically detects modifications made to managed entities.

Example

```java
Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class,101);

employee.setSalary(100000);

tx.commit();
```

No explicit update statement is required.

Hibernate compares:

```text
Original Entity
        vs
Modified Entity
```

and generates the SQL UPDATE statement automatically.

This feature works because the entity is managed by the Persistence Context.

---

# 8. Repository Layer Basics

Writing all database operations inside the `Main` class is suitable for learning but not for production applications.

The Repository Layer separates persistence logic from business logic.

Current structure

```text
Main
 |
CRUD Logic
 |
Database
```

Improved structure

```text
Main
 |
EmployeeRepository
 |
Database
```

Example

```java
public class EmployeeRepository {

    public Employee findById(int id) {
        ...
    }

    public List<Employee> findAll() {
        ...
    }

    public void save(Employee employee) {
        ...
    }

    public void update(Employee employee) {
        ...
    }

    public void delete(Employee employee) {
        ...
    }
}
```

---

# Key Takeaways

```text
Hibernate
    |
Native APIs

save()
get()
update()
delete()

↓

JPA

persist()
find()
merge()
remove()

↓

Persistence Context

↓

First-Level Cache

↓

Dirty Checking

↓

Repository Pattern
```

## Hands-on Exercises

1. Perform CRUD operations using Hibernate native APIs.
2. Replace Hibernate APIs with JPA APIs while keeping the entity class unchanged.
3. Compare `Session` and `EntityManager`.
4. Observe generated SQL using `hibernate.show_sql=true`.
5. Retrieve the same entity twice in the same Session and observe the First-Level Cache.
6. Modify a managed entity without calling `update()` and observe Dirty Checking.
7. Refactor CRUD operations from the `Main` class into an `EmployeeRepository`.
