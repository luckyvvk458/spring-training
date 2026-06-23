package com.training.model;

import javax.validation.constraints.*;

public class Employee {

    @NotBlank(message = "Employee Name cannot be empty")
    private String name;

    @Email(message = "Invalid Email Address")
    private String email;

    @Min(value = 18,
            message = "Age must be greater than or equal to 18")
    private int age;

    @NotBlank(message = "Department cannot be empty")
    private String department;

    @DecimalMin(value = "10000",
            message = "Salary should be at least 10000")
    private double salary;

    public Employee() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                '}';
    }
}