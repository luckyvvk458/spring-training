# Spring Framework - Day 4 Notes

# JDBC Revision, Spring JDBC, DAO Pattern and CRUD Operations

## Learning Objectives

By the end of this session, participants should be able to:

* Understand JDBC Architecture
* Connect Java Applications to Databases
* Understand Statement and PreparedStatement
* Understand problems with traditional JDBC
* Understand Spring JDBC
* Understand JdbcTemplate
* Understand DataSource
* Understand RowMapper
* Understand DAO Pattern
* Perform CRUD Operations using Spring JDBC

---

# Session 1 - JDBC Revision

## Why Do We Need a Database?

Suppose we create:

```java
Student student =
        new Student(101, "Rahul");
```

Where is the data stored?

Inside JVM memory.

What happens when application stops?

```text
Data is lost.
```

To store data permanently we need:

```text
Database
```

Examples:

* MySQL
* PostgreSQL
* Oracle
* SQL Server

---

## What is JDBC?

JDBC stands for:

```text
Java Database Connectivity
```

JDBC is an API provided by Java to communicate with databases.

---

## JDBC Architecture

```text
Java Application
       |
       |
      JDBC
       |
       |
Database Driver
       |
       |
    Database
```

---

## Loading Driver

```java
Class.forName(
        "com.mysql.cj.jdbc.Driver");
```

Purpose:

* Loads MySQL Driver
* Registers Driver with DriverManager

---

## DriverManager

```java
DriverManager.getConnection(...)
```

Think of DriverManager as a receptionist.

Application asks:

```text
I need a MySQL Connection.
```

DriverManager identifies the correct driver and creates the connection.

---

## Connection

```java
Connection con
```

Represents an active session between Java Application and Database.

---

## Statement

```java
Statement statement =
        con.createStatement();
```

Used to execute SQL statements.

---

## ResultSet

```java
ResultSet resultSet
```

Represents records returned by database.

Example:

```text
id     name

1      Rahul
2      Sai
3      Ravi
```

---

## Mapping Records to Objects

```java
while(resultSet.next()) {

    Student student =
            new Student(
                    resultSet.getInt("id"),
                    resultSet.getString("name"));

    list.add(student);
}
```

Conversion:

```text
Database Row
      ↓
Student Object
```

---

## Problems with Statement

Example:

```java
String sql =
        "select * from student where id="
                + id;
```

Problems:

* String Concatenation
* SQL Injection
* Difficult Maintenance

---

## PreparedStatement

```java
PreparedStatement ps =
        con.prepareStatement(
                "select * from student where id=?");
```

Placeholder:

```text
?
```

Value provided later.

```java
ps.setInt(1, id);
```

---

## Benefits of PreparedStatement

* Prevents SQL Injection
* Better Performance
* Cleaner Code
* Preferred in Real Projects

---

## Try-With-Resources

Traditional JDBC:

```java
finally {
    resultSet.close();
    statement.close();
    connection.close();
}
```

Improved Version:

```java
try(
        Connection con = ...;
        PreparedStatement ps = ...;
        ResultSet rs = ...
) {

}
```

Resources are automatically closed.

---

## Problems Still Present

Even after PreparedStatement and Try-With-Resources:

```text
Connection Creation

PreparedStatement Creation

Exception Handling

ResultSet Processing

Resource Management
```

Still repeated everywhere.

This leads to Spring JDBC.

---

# Session 2 - Spring JDBC & JdbcTemplate

## What is Spring JDBC?

Spring JDBC is a module provided by Spring Framework that simplifies database access.

---

## Components of Spring JDBC

```text
Spring JDBC
     |
     +-- JdbcTemplate
     +-- NamedParameterJdbcTemplate
     +-- SimpleJdbcInsert
     +-- SimpleJdbcCall
     +-- RowMapper
     +-- DataAccessException
```

---

## What is JdbcTemplate?

JdbcTemplate is the core class of Spring JDBC.

Purpose:

```text
Reduce JDBC Boilerplate Code
```

---

## What Does JdbcTemplate Handle?

JdbcTemplate internally performs:

```text
Get Connection
      ↓
Create PreparedStatement
      ↓
Set Parameters
      ↓
Execute SQL
      ↓
Read ResultSet
      ↓
Map Objects
      ↓
Handle Exceptions
      ↓
Close Resources
```

---

## Traditional JDBC

```java
Connection con =
        DriverManager.getConnection(...);

PreparedStatement ps =
        con.prepareStatement(...);

ResultSet rs =
        ps.executeQuery();
```

---

## Spring JDBC

```java
jdbcTemplate.query(...);
```

Much cleaner.

---

## DataSource

Traditional JDBC:

```java
DriverManager.getConnection(...)
```

Spring JDBC:

```java
dataSource.getConnection()
```

---

## What is DataSource?

DataSource is an interface.

```java
public interface DataSource {

    Connection getConnection();
}
```

Provides database connections.

---

## DataSource Implementations

Examples:

* DriverManagerDataSource
* HikariCP
* Apache DBCP
* C3P0

---

## Real World Flow

```text
JdbcTemplate
      ↓
DataSource
      ↓
Connection Pool
      ↓
Database
```

---

## Java Configuration

```java
@Bean
public DataSource dataSource() {

    DriverManagerDataSource ds =
            new DriverManagerDataSource();

    ds.setDriverClassName(
            "com.mysql.cj.jdbc.Driver");

    ds.setUrl(
            "jdbc:mysql://localhost:3306/jdbc_training");

    ds.setUsername("root");
    ds.setPassword("root");

    return ds;
}
```

---

## JdbcTemplate Bean

```java
@Bean
public JdbcTemplate jdbcTemplate(
        DataSource dataSource) {

    return new JdbcTemplate(
            dataSource);
}
```

Dependency Injection is happening here.

---

## Insert Example

```java
String sql =
        "insert into student(id,name) values(?,?)";

jdbcTemplate.update(
        sql,
        student.getId(),
        student.getName());
```

---

## RowMapper

Purpose:

```text
Database Row
      ↓
Java Object
```

---

## Custom RowMapper

```java
public class StudentRowMapper
        implements RowMapper<Student> {

    @Override
    public Student mapRow(
            ResultSet rs,
            int rowNum)
            throws SQLException {

        return new Student(
                rs.getInt("id"),
                rs.getString("name"));
    }
}
```

---

## Teaching Analogy

JdbcTemplate is like an experienced assistant.

You only provide:

* SQL Query
* Parameters
* Mapping Logic

JdbcTemplate handles everything else.

---

# Session 3 - DAO Pattern

## Problem Statement

Bad Design:

```java
@Service
public class StudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
```

StudentService contains:

* Business Logic
* Database Logic

Both mixed together.

---

## What is DAO?

DAO stands for:

```text
Data Access Object
```

Definition:

DAO is a design pattern used to separate database access logic from business logic.

---

## Real World Analogy

```text
Customer
    ↓
Waiter
    ↓
Kitchen
```

Customer never talks directly to kitchen.

Similarly:

```text
Service
    ↓
DAO
    ↓
Database
```

---

## Layered Architecture

```text
Controller
      ↓
Service
      ↓
DAO
      ↓
Database
```

---

## DAO Interface

```java
public interface StudentDAO {

    void save(Student student);

    List<Student> findAll();

    void update(Student student);

    void delete(int id);
}
```

---

## DAO Implementation

```java
@Repository
public class StudentDAOImpl
        implements StudentDAO {
}
```

---

## Service Layer

```java
@Service
public class StudentService {

    @Autowired
    private StudentDAO studentDAO;
}
```

---

## Responsibility Separation

Service Layer:

```text
What should happen?
```

Examples:

* Register Student
* Calculate Marks
* Send Notification

---

DAO Layer:

```text
How data is stored?
```

Examples:

* Insert Student
* Update Student
* Delete Student
* Fetch Student

---

## Benefits of DAO

* Separation of Concerns
* Better Maintainability
* Easier Testing
* Better Architecture
* Loose Coupling

---

# Session 4 - CRUD Operations using Spring JDBC

## What is CRUD?

```text
C = Create

R = Read

U = Update

D = Delete
```

---

## StudentDAO Interface

```java
public interface StudentDAO {

    void save(Student student);

    List<Student> findAll();

    void update(Student student);

    void delete(int id);
}
```

---

## Create Operation

```java
@Override
public void save(Student student) {

    String sql =
            "insert into student values(?,?)";

    jdbcTemplate.update(
            sql,
            student.getId(),
            student.getName());
}
```

---

## Read Operation

```java
@Override
public List<Student> findAll() {

    String sql =
            "select * from student";

    return jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                    new Student(
                            rs.getInt("id"),
                            rs.getString("name")
                    ));
}
```

---

## Update Operation

```java
@Override
public void update(Student student) {

    String sql =
            "update student set name=? where id=?";

    jdbcTemplate.update(
            sql,
            student.getName(),
            student.getId());
}
```

---

## Delete Operation

```java
@Override
public void delete(int id) {

    String sql =
            "delete from student where id=?";

    jdbcTemplate.update(
            sql,
            id);
}
```

---

## Complete Flow

```text
StudentService
        ↓
StudentDAO
        ↓
JdbcTemplate
        ↓
DataSource
        ↓
Database
```

---

## Evolution of Database Access

```text
Traditional JDBC

DriverManager
Connection
PreparedStatement
ResultSet

        ↓

Spring JDBC

JdbcTemplate

        ↓

DAO Pattern

StudentDAO

        ↓

CRUD Operations

Create
Read
Update
Delete
```

---

# End-of-Day Assessment

## Theory Questions

1. What is JDBC?
2. What is DriverManager?
3. What is Connection?
4. Difference between Statement and PreparedStatement?
5. What is SQL Injection?
6. Why is PreparedStatement preferred?
7. What is Spring JDBC?
8. Difference between Spring JDBC and JdbcTemplate?
9. What is DataSource?
10. Difference between DriverManager and DataSource?
11. What is JdbcTemplate?
12. What does JdbcTemplate do internally?
13. What is RowMapper?
14. Why do we need RowMapper?
15. What is DAO Pattern?
16. Why do we need DAO?
17. Difference between Service Layer and DAO Layer?
18. What is CRUD?
19. Difference between query() and update()?
20. Explain the complete flow of Spring JDBC application.

---

# Hands-On Assignment

## Employee Management System

Create:

```text
Employee

EmployeeDAO

EmployeeDAOImpl

AppConfig

Main Class
```

Implement:

* Add Employee
* View All Employees
* Update Employee
* Delete Employee

Mandatory Requirements:

* Spring JDBC
* JdbcTemplate
* DataSource
* DAO Pattern
* CRUD Operations
* RowMapper

---

## Expected Learning Outcome

Students should be able to:

* Connect Java applications to databases
* Use JdbcTemplate
* Create DataSource beans
* Implement DAO Pattern
* Perform CRUD Operations
* Map database rows to Java objects using RowMapper
* Build Spring JDBC applications using layered architecture

```
```
