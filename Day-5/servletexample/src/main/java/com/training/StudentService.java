package com.training;

import org.springframework.stereotype.Service;

@Service
public class StudentService {

    public Student getStudent() {

        Student s = new Student();
        s.setId(101);
        s.setName("Vivek");

        return s;
    }

    public void saveStudent(Student student) {

        System.out.println(
                "Saving Student : "
                        + student.getId()
                        + " "
                        + student.getName()
        );
    }
}
