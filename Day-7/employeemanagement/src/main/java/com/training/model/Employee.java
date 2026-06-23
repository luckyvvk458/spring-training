package com.training.model;

import org.springframework.lang.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class Employee {
    @Min(value = 1, message = "Id can not be zero or negative ")
    private int id;
    @NotBlank(message = "Name Cannot be blank")
    private String name;
    @Min(value = 1000, message = "Salary cannot be less than 1000")
    private double salary;
    @NotBlank(message = "City cannot be blank")
    private String city;
    @NotBlank(message = "Department cannot be blank")
    private String department;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
