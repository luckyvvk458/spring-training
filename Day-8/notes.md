# Day 8 Notes - Validation & Hibernate Fundamentals

# Session 1 - RequestBody, Validation & Binding

## Request Processing Flow

When a request hits a Spring MVC Controller:

```text
HTTP Request
     ↓
DispatcherServlet
     ↓
Handler Mapping
     ↓
Controller Method
     ↓
@RequestBody
     ↓
Jackson
     ↓
Java Object
     ↓
@Valid
     ↓
Hibernate Validator
     ↓
Controller Logic
```

---

## @RequestBody

Used to convert JSON request payload into a Java Object.

Example:

```json
{
    "id": 101,
    "name": "Vivek",
    "salary": 50000
}
```

Controller:

```java
@PostMapping("/save")
@ResponseBody
public String saveEmployee(
        @RequestBody Employee employee) {

    return employee.toString();
}
```

### Internally

```text
JSON
 ↓
Jackson ObjectMapper
 ↓
Employee Object
```

---

## Why Jackson Dependency?

Without:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

Spring cannot convert:

```json
{
  "id":1
}
```

into:

```java
Employee
```

Result:

```text
415 Unsupported Media Type
```

---

## @ModelAttribute vs @RequestBody

### @ModelAttribute

Works with:

```text
application/x-www-form-urlencoded
```

Request:

```text
id=1&name=Vivek
```

Binding:

```text
Request Parameters
        ↓
Setter Injection
        ↓
Employee Object
```

---

### @RequestBody

Works with:

```text
application/json
```

Request:

```json
{
  "id":1,
  "name":"Vivek"
}
```

Binding:

```text
JSON
    ↓
Jackson
    ↓
Employee Object
```

---

## Validation Using @Valid

Model:

```java
public class Employee {

    @Positive(message = "Id must be positive")
    private int id;

    @NotBlank(message = "Name cannot be blank")
    private String name;
}
```

Controller:

```java
@PostMapping("/save")
public String save(
        @Valid @RequestBody Employee employee,
        BindingResult result) {

    if(result.hasErrors()) {
        return "Validation Failed";
    }

    return "Success";
}
```

---

## Validation Execution Flow

```text
JSON Request
      ↓
Jackson Creates Employee Object
      ↓
@Valid
      ↓
Hibernate Validator
      ↓
Checks Every Validation Annotation
      ↓
Collects All Violations
```

Important:

Validation happens AFTER object creation.

---

## Example

Request:

```json
{
    "id": -10,
    "name": ""
}
```

Violations:

```text
id : Id must be positive

name : Name cannot be blank
```

Both validations execute.

Validation does not stop at first failure.

---

## BindingResult

Used to capture validation errors.

```java
if(result.hasErrors()) {

    return result.getFieldErrors()
            .stream()
            .map(error ->
                error.getField() +
                " : " +
                error.getDefaultMessage())
            .collect(Collectors.joining(", "));
}
```

Sample Output:

```text
id : Id must be positive,
name : Name cannot be blank
```

---

## Parsing Error vs Validation Error

Request:

```json
{
    "id":"abc",
    "name":""
}
```

Model:

```java
private int id;
```

Result:

```text
Jackson Parsing Error
```

Validation never executes.

Flow:

```text
JSON Parsing
      ↓
Fails
      ↓
No Object Created
      ↓
No Validation
```

---

# Session 2 - Hibernate Fundamentals

## Why Hibernate?

JDBC:

```java
Connection
PreparedStatement
ResultSet
```

Problems:

* Boilerplate Code
* Manual Object Mapping
* SQL Everywhere
* Relationship Handling Difficult

---

## ORM

ORM = Object Relational Mapping

```text
Java Class
      ↓
Database Table

Java Object
      ↓
Database Row

Java Field
      ↓
Database Column
```

Example:

```java
Employee
```

maps to

```sql
employee
```

table.

---

## Hibernate Architecture

```text
Application
      ↓
SessionFactory
      ↓
Session
      ↓
Database
```

---

## SessionFactory

Responsibilities:

* Reads hibernate.cfg.xml
* Reads Entity Metadata
* Creates Hibernate Configuration
* Heavyweight Object

Best Practice:

```text
One SessionFactory Per Database
```

Usually stored as a static singleton-like object.

---

## Session

Represents communication with Database.

Characteristics:

```text
Lightweight
Not Thread Safe
Short Lived
```

Example:

```java
Session session =
    sessionFactory.openSession();
```

---

## Hibernate Configuration

File:

```text
hibernate.cfg.xml
```

Contains:

* Driver
* URL
* Username
* Password
* Dialect
* Entity Mapping

Example:

```xml
<mapping class="com.training.Employee"/>
```

---

## Entity

```java
@Entity
@Table(name = "employee")
public class Employee {
}
```

Entity represents a database table.

---

## Retrieve Single Record

```java
Employee employee =
        session.get(Employee.class, 101);
```

Generated SQL:

```sql
select *
from employee
where id = 101;
```

Important:

If record doesn't exist:

```java
employee == null
```

No Exception.

---

## Retrieve All Records

```java
List<Employee> employees =
        session.createQuery(
            "from Employee",
            Employee.class)
            .list();
```

Important:

HQL uses:

```text
Entity Name
```

not

```text
Table Name
```

Correct:

```java
from Employee
```

Wrong:

```java
from employee
```

---

## Save Record

```java
Transaction tx =
        session.beginTransaction();

Employee employee =
        new Employee();

employee.setId(201);
employee.setName("Vivek");
employee.setSalary(50000);

session.save(employee);

tx.commit();
```

Generated SQL:

```sql
insert into employee ...
```

---

## Update Record

```java
Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class, 201);

employee.setSalary(70000);

session.update(employee);

tx.commit();
```

Generated SQL:

```sql
update employee
set salary = 70000
where id = 201;
```

Note:

If employee was loaded using same session:

```java
session.update(...)
```

is not required.

Hibernate performs Dirty Checking.

---

## Delete Record

```java
Transaction tx =
        session.beginTransaction();

Employee employee =
        session.get(Employee.class, 201);

session.delete(employee);

tx.commit();
```

Generated SQL:

```sql
delete
from employee
where id = 201;
```

---

## Transaction

Required for:

```text
Insert
Update
Delete
```

Generally used for:

```java
Transaction tx =
        session.beginTransaction();

...

tx.commit();
```

---

## Recommended Pattern

```java
try {

    Transaction tx =
            session.beginTransaction();

    ...

    tx.commit();

}
catch(Exception e) {

    tx.rollback();

}
```

---

## Important Interview Questions

1. Why do we need Hibernate when JdbcTemplate exists?
2. What is ORM?
3. Difference between SessionFactory and Session?
4. Why is SessionFactory a Singleton-like object?
5. Difference between Table Name and Entity Name in HQL?
6. What happens when session.get() cannot find a record?
7. Why is transaction required for save/update/delete?
8. What is Dirty Checking?
