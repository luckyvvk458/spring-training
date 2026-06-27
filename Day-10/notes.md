# Spring Data JPA -Notes

# Learning Outcomes

By the end of this session, you should be able to:

* Explain why Spring Data JPA was introduced.
* Identify the limitations of writing CRUD operations using plain JPA.
* Understand the Repository Pattern.
* Explain how Spring Data JPA generates repository implementations automatically.
* Perform CRUD operations using `JpaRepository`.
* Implement Pagination.
* Implement Sorting.
* Combine Pagination and Sorting in a Spring Data JPA application.

---

# Learning Journey

```
JPA CRUD

↓

Repository Pattern

↓

Spring Data JPA

↓

Pagination

↓

Sorting
```

In this session, we will focus on replacing manually written JPA repository code with Spring Data JPA. The concepts of Spring MVC and Spring Boot are outside the scope of this session.

---

# 1. Revisiting JPA CRUD Operations

Before learning Spring Data JPA, let's recall how CRUD operations were performed using JPA.

### Saving an Employee

```java
EntityManager em = emf.createEntityManager();

EntityTransaction tx = em.getTransaction();

tx.begin();

em.persist(employee);

tx.commit();
```

### Finding an Employee

```java
Employee employee = em.find(Employee.class, id);
```

### Updating an Employee

```java
em.merge(employee);
```

### Deleting an Employee

```java
em.remove(employee);
```

---

## Reflection

While using JPA, who is responsible for the following?

* Creating the `EntityManager`
* Beginning the transaction
* Committing the transaction
* Closing the `EntityManager`

**Answer**

The application developer is responsible for performing all these tasks manually.

This works well, but as applications grow, the amount of repetitive code also increases.

---

# 2. Understanding the Repository Pattern

To make our application more organized, we introduce the Repository Pattern.

## Initial Architecture

```
Main

↓

EntityManager

↓

Database
```

The `Main` class directly interacts with the `EntityManager`, making it responsible for database operations.

---

## Improved Architecture

```
Main

↓

EmployeeRepository

↓

EmployeeRepositoryImpl

↓

EntityManager

↓

Database
```

Now, the business code communicates only with the repository.

The repository implementation communicates with JPA.

---

## Repository Interface

```java
public interface EmployeeRepository {

    void save(Employee employee);

    Employee findById(int id);

    List<Employee> findAll();

    Employee merge(Employee employee);

    void delete(int id);

}
```

---

## Repository Implementation

```java
public Employee findById(int id) {
    return entityManager.find(Employee.class, id);
}
```

---

## Using the Repository

```java
EmployeeRepository repository =
        new EmployeeRepositoryImpl();

Employee employee =
        repository.findById(1);

System.out.println(employee);
```

The `Main` class no longer depends directly on the `EntityManager`.

---

## Reflection

Who implemented `EmployeeRepository`?

**Answer**

We manually created `EmployeeRepositoryImpl`.

---

# 3. Identifying the Problem

Now, examine the implementation of `EmployeeRepositoryImpl`.

Notice what every method is doing.

### Save

```java
entityManager.persist(employee);
```

### Find

```java
entityManager.find(Employee.class, id);
```

### Delete

```java
entityManager.remove(employee);
```

Every repository method simply delegates its work to the `EntityManager`.

Imagine an application containing:

* EmployeeRepository
* StudentRepository
* DepartmentRepository
* CustomerRepository
* ProductRepository
* OrderRepository

Each repository would require the same CRUD methods to be written repeatedly.

This repetition is known as **boilerplate code**.

Spring Data JPA was created to eliminate this repetitive work.

---

# 4. Introducing Spring Data JPA

Instead of writing a repository implementation manually, Spring Data JPA only requires an interface.

```java
public interface EmployeeRepository
        extends JpaRepository<Employee, Integer> {

}
```

Notice that there is **no** `EmployeeRepositoryImpl`.

---

## Observe

The following code still works:

```java
EmployeeRepository repository =
        context.getBean(EmployeeRepository.class);

System.out.println(repository);
```

Even though no implementation class exists, Spring provides an object.

---

## How Does This Work?

During application startup, Spring scans all interfaces extending `JpaRepository`.

It automatically generates an implementation for each repository.

This generated implementation internally uses the `EntityManager` to communicate with the database.

---

# 5. CRUD Operations Using Spring Data JPA

Instead of writing:

```java
entityManager.persist(employee);
```

Use:

```java
repository.save(employee);
```

---

Instead of:

```java
entityManager.find(Employee.class, 1);
```

Use:

```java
repository.findById(1);
```

---

Instead of:

```java
TypedQuery<Employee> query =
        entityManager.createQuery(...);
```

Use:

```java
repository.findAll();
```

---

Delete an employee:

```java
repository.deleteById(1);
```

---

## Important

Although you are calling repository methods, the actual database operations are still performed by JPA using the `EntityManager`.

Spring Data JPA simplifies the developer experience by generating the implementation automatically.

---

# 6. Pagination

Imagine the `Employee` table contains one million records.

Calling

```java
repository.findAll();
```

would load every record into memory.

This is inefficient.

Instead, retrieve only the required page.

---

## Creating a Page Request

```java
Pageable pageable =
        PageRequest.of(0, 5);

Page<Employee> page =
        repository.findAll(pageable);
```

Where:

* `0` → Page Number
* `5` → Number of records per page

---

## Reading Page Information

```java
page.getContent()

page.getNumber()

page.getSize()

page.getTotalPages()

page.getTotalElements()

page.hasNext()

page.hasPrevious()
```

---

### Example

```
PageRequest.of(0,5)
```

Returns:

```
Rows 1–5
```

---

```
PageRequest.of(1,5)
```

Returns:

```
Rows 6–10
```

---

```
PageRequest.of(2,5)
```

Returns:

```
Rows 11–15
```

---

## Real-World Example

In a web application, page information usually comes from the user interface.

Example:

```
GET /employees?page=2&size=5
```

The application converts these values into:

```java
PageRequest.of(page, size)
```

---

# 7. Sorting

Suppose employees should be displayed according to their salary.

Using JPA:

```java
SELECT e
FROM Employee e
ORDER BY salary
```

Using Spring Data JPA:

```java
repository.findAll(
        Sort.by("salary")
);
```

---

## Descending Order

```java
repository.findAll(
        Sort.by("salary")
                .descending()
);
```

---

## Sort by Name

```java
repository.findAll(
        Sort.by("name")
);
```

---

## Multiple Sorting

```java
repository.findAll(
        Sort.by("salary")
                .and(Sort.by("name"))
);
```

This means:

1. Sort by salary.
2. If two employees have the same salary, sort them by name.

---

# 8. Combining Pagination and Sorting

Both features can be used together.

```java
Pageable pageable =
        PageRequest.of(
                0,
                5,
                Sort.by("salary").descending()
        );

Page<Employee> page =
        repository.findAll(pageable);
```

Execution order:

```
Sorting

↓

Pagination
```

The records are first sorted and then divided into pages.

---

# Key Differences

## JPA

* CRUD methods are written manually.
* Repository implementation must be created by the developer.
* Developers interact directly with the `EntityManager`.
* Transaction management is handled explicitly.

---

## Spring Data JPA

* Repository implementation is generated automatically.
* Eliminates repetitive CRUD code.
* Provides built-in pagination.
* Provides built-in sorting.
* Supports automatic query generation.
* Internally uses JPA and the `EntityManager`.

---

# Evolution of Database Access

```
JDBC

↓

Hibernate

↓

JPA

↓

Repository Pattern

↓

Spring Data JPA
```

Every stage in this evolution reduces the amount of boilerplate code while improving developer productivity.

---

# Practice Exercises

Complete the following exercises to reinforce your understanding.

### Exercise 1

Refactor the existing JPA application using the Repository Pattern.

---

### Exercise 2

Create the `EmployeeRepository` interface.

---

### Exercise 3

Implement `EmployeeRepositoryImpl` using the `EntityManager`.

---

### Exercise 4

Verify all CRUD operations.

---

### Exercise 5

Delete `EmployeeRepositoryImpl`.

---

### Exercise 6

Extend `JpaRepository`.

---

### Exercise 7

Verify CRUD operations again.

---

### Exercise 8

Insert at least 15 employee records.

---

### Exercise 9

Implement pagination using `PageRequest`.

---

### Exercise 10

Implement sorting using `Sort.by()`.

---

### Exercise 11

Combine pagination and sorting.

---

# Self-Check Questions

Test your understanding by answering the following questions.

1. Why was the Repository Pattern introduced?
2. What repetitive code did we write while using JPA?
3. Who creates the repository implementation in Spring Data JPA?
4. Does Spring Data JPA replace JPA?
5. Does `JpaRepository` internally use the `EntityManager`?
6. Why is pagination important?
7. Why do page numbers start from zero?
8. Where do page number and page size come from in a real application?
9. What SQL is generated for sorting?
10. What SQL is generated for pagination?

---

# Final Architecture

```
Main

↓

EmployeeRepository

↓

Spring Generated Repository

↓

EntityManager

↓

Hibernate

↓

MySQL
```

---

# What You Should Remember

By the end of this session, you should clearly understand that:

* Spring Data JPA is built on top of JPA.
* It does not replace JPA.
* Spring automatically generates repository implementations.
* CRUD boilerplate code is eliminated.
* Pagination and sorting are available out of the box.
* Internally, Spring Data JPA still uses the `EntityManager` to perform all database operations.
