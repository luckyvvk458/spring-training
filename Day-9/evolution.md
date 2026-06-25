# Evolution of Database Access in Java

## Phase 1 - JDBC

### Why JDBC was introduced?

JDBC provides a standard API to communicate with relational databases.

Example:

```java
Connection connection = DriverManager.getConnection(...);

PreparedStatement ps =
        connection.prepareStatement(
                "SELECT * FROM employee");

ResultSet rs = ps.executeQuery();

while(rs.next()) {

    Employee employee = new Employee();

    employee.setId(rs.getInt("id"));
    employee.setName(rs.getString("name"));
    employee.setSalary(rs.getDouble("salary"));
}

rs.close();
ps.close();
connection.close();
```

### Problems with JDBC

* Large amount of boilerplate code
* Manual resource management
* SQL scattered throughout the application
* Manual mapping between ResultSet and Java objects
* Manual transaction management
* Error-prone and difficult to maintain

---

# Phase 2 - Spring JdbcTemplate

Spring introduced JdbcTemplate to reduce JDBC boilerplate.

Example

```java
String sql =
        "SELECT * FROM employee";

List<Employee> employees =
        jdbcTemplate.query(
                sql,
                new EmployeeRowMapper());
```

Resource management is now handled by Spring.

### Improvements

* No manual Connection handling
* No manual PreparedStatement handling
* No manual ResultSet closing
* Better exception handling

### Remaining Problems

SQL is still written manually.

```java
SELECT * FROM employee
```

Every query must still be maintained.

Object mapping is still required.

```java
public class EmployeeRowMapper
        implements RowMapper<Employee> {

    public Employee mapRow(...) {

        Employee employee = new Employee();

        employee.setId(...);
        employee.setName(...);

        return employee;
    }
}
```

Whenever a new column is added, the RowMapper must also be updated.

---

# Phase 3 - Hibernate

Hibernate introduced Object Relational Mapping (ORM).

Instead of thinking in terms of tables, developers think in terms of Java objects.

Example

```java
Employee employee =
        session.get(Employee.class,101);
```

Hibernate automatically performs:

* SQL generation
* ResultSet processing
* Object creation
* Field mapping

The developer simply receives an Employee object.

---

## What Hibernate Eliminates

Instead of writing

```java
SELECT *
FROM employee
WHERE id=101
```

developers write

```java
session.get(Employee.class,101);
```

Instead of

```java
Employee employee = new Employee();

employee.setId(...);
employee.setName(...);
employee.setSalary(...);
```

Hibernate creates and populates the object automatically.

---

# "Why does Hibernate still look like more code?"

This is a very common question.

Example

```java
Session session =
        factory.openSession();

Transaction tx =
        session.beginTransaction();

session.save(employee);

tx.commit();

session.close();
```

At first glance this appears longer than JdbcTemplate.

However, the responsibilities are completely different.

JdbcTemplate executes one SQL statement.

Hibernate manages an entire Persistence Context.

---

# Hibernate is not just executing SQL

When Hibernate executes

```java
session.save(employee);
```

it is also responsible for

* Entity Lifecycle Management
* Persistence Context
* First-Level Cache
* Dirty Checking
* Relationship Management
* Lazy Loading
* Cascade Operations
* Flush Management
* Optimistic Locking
* SQL Generation
* Object Mapping

These are responsibilities that JdbcTemplate does not provide.

---

# Responsibility Comparison

## JdbcTemplate

```text
Execute SQL
↓
Map ResultSet
↓
Return Object
```

Once the object is returned, JdbcTemplate has no further responsibility.

---

## Hibernate

```text
Manage Entity
↓
Track Changes
↓
Synchronize with Database
↓
Manage Cache
↓
Generate SQL
↓
Manage Relationships
```

Hibernate continues managing the object until the Session ends.

---

# Why Session Exists

The Session is much more than a database connection.

It represents a Unit of Work.

```text
Session
    |
Persistence Context
    |
Managed Entities
```

Without Session, features such as

* Dirty Checking
* First-Level Cache
* Lazy Loading

would not be possible.

---

# A Fair Comparison

## JdbcTemplate

```java
String sql =
        "SELECT * FROM employee";

jdbcTemplate.query(sql,rowMapper);
```

Responsibility:

```text
Execute SQL
```

---

## Hibernate

```java
Employee employee =
        session.get(Employee.class,101);
```

Responsibility:

```text
Generate SQL
Create Object
Populate Object
Track Changes
Cache Object
Manage Lifecycle
```

Although the Hibernate code appears slightly longer because of Session and Transaction management, it performs significantly more work internally.


# Evolution Summary

```text
JDBC
│
├── Manual SQL
├── Manual Connection
├── Manual Object Mapping
├── Manual Transactions
│
▼
JdbcTemplate
│
├── Less Boilerplate
├── SQL Still Required
├── Manual RowMapper
│
▼
Hibernate
│
├── No Manual SQL (most cases)
├── Automatic Object Mapping
├── Persistence Context
├── Entity Lifecycle
├── Dirty Checking
├── First-Level Cache
├── Relationship Management
│
▼
```
