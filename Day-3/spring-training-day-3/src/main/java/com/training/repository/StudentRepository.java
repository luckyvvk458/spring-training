package com.training.repository;

import com.training.model.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {
    public void save(Student student) {
        System.out.println("Saving student " + student.getName());
    }
}
