# Day 6 - Curiosity Corner

These are discussion points that help students understand what Spring MVC is doing internally rather than just memorizing annotations.

---

# Curiosity 1: How Does Spring Know Which Controller Method To Execute?

```java
@GetMapping
public String getAllStudents() {
    return "All Students";
}

@GetMapping("/{id}")
public String getStudentById(
        @PathVariable int id) {

    return "Student : " + id;
}
```

When the user accesses:

```text
/student/101
```

Question:

```text
How does Spring know that getStudentById()
should be executed?
```

Answer:

```text
Application Startup

      ↓

Spring Scans Controllers

      ↓

Builds URL Mapping Table

      ↓

DispatcherServlet Uses Mapping
```

Internally Spring maintains something similar to:

```text
/student       → getAllStudents()

/student/{id}  → getStudentById()
```

---

# Curiosity 2: Everything In URL Is Text. How Did It Become int?

```java
@GetMapping("/{id}")
public String getStudent(
        @PathVariable int id) {

    return "Student";
}
```

URL:

```text
/student/101
```

Question:

```text
101 comes from URL.

URL contains String.

How did it become int?
```

Answer:

```text
Spring Conversion Service
```

Internally:

```java
Integer.parseInt("101");
```

is performed automatically.

---

# Curiosity 3: Does @PathVariable Need To Match Model Field Name?

```java
@GetMapping("/{id}")
public String getStudent(
        @PathVariable("id")
        int studentId) {

    return "Student : " + studentId;
}
```

Question:

```text
Should studentId match
Student class field name?
```

Answer:

```text
No
```

@PathVariable only maps:

```text
URL Placeholder
      ↓
Method Parameter
```

Example:

```java
@GetMapping("/{id}")
public String getStudent(
        @PathVariable("id")
        int banana) {

    return "Student : " + banana;
}
```

Still works.

---

# Curiosity 4: What Happens If Two Methods Match The Same URL?

```java
@GetMapping("/{id}")
public String method1() {
    return "Method1";
}

@GetMapping("/{name}")
public String method2() {
    return "Method2";
}
```

Question:

```text
Which method gets executed?
```

Answer:

```text
None
```

Spring fails during application startup.

Reason:

```text
{id}
{name}
```

are only variable names.

Actual URL pattern is identical.

Spring throws:

```text
Ambiguous Mapping Exception
```

---

# Curiosity 5: Who Calls The Setter Methods In @ModelAttribute?

```java
@PostMapping
public String save(
        @ModelAttribute Employee employee) {

    return "Success";
}
```

Question:

```text
We never wrote:

employee.setName(...)
employee.setAge(...)

So who called them?
```

Answer:

```text
Spring Framework
```

using:

```text
Reflection
```

Internally:

```java
Employee employee = new Employee();

employee.setName("Vivek");
employee.setAge(30);
```

is happening automatically.

---

# Curiosity 6: Why Does @ModelAttribute Need A Default Constructor?

```java
public class Employee {

    public Employee() {
    }
}
```

Question:

```text
Why does Spring need
a no-argument constructor?
```

Answer:

Spring first creates:

```java
Employee employee =
        new Employee();
```

Then performs binding:

```java
employee.setName("Vivek");
employee.setAge(30);
```

Without object creation, data binding cannot happen.

---

# Curiosity 7: Why Doesn't Spring Use Constructor Injection For Forms?

Question:

```text
Why not directly create:

new Employee(
    name,
    email,
    age
);
```

Answer:

Forms may contain:

```text
Missing Fields
Invalid Values
Partial Data
```

Spring prefers:

```text
Create Object

↓

Bind Available Data

↓

Collect Errors

↓

Store Errors
```

instead of failing immediately.

---

# Curiosity 8: What Happens If Setter Is Missing?

```java
public class Employee {

    private int age;

    public int getAge() {
        return age;
    }
}
```

Question:

```text
Can Spring populate age?
```

Answer:

```text
No
```

Spring cannot execute:

```java
employee.setAge(...)
```

because setter does not exist.

Binding may fail.

---

# Curiosity 9: Is BindingResult Only For Validation Errors?

Most developers initially think:

```text
BindingResult
      ↓
Validation Errors Only
```

Actually it stores:

```text
1. Binding Errors

2. Validation Errors
```

Binding Error Example:

```text
age=abc
```

cannot be converted to:

```java
int age
```

Validation Error Example:

```java
@Min(18)
private int age;
```

Input:

```text
age=10
```

Binding succeeds.

Validation fails.

Both are available inside:

```java
BindingResult
```

---

# Golden Question

Browser sends:

```text
Strings Only
```

Spring receives:

```text
Strings Only
```

Then how do we finally get:

```java
@PathVariable int id

@ModelAttribute Employee employee
```

without writing any conversion code?

Answer:

```text
Spring MVC Internals

DispatcherServlet
+ Handler Mapping
+ Reflection
+ Data Binding
+ Conversion Service
+ Validation
```

Understanding this flow is the first step towards understanding how Spring MVC works internally.
