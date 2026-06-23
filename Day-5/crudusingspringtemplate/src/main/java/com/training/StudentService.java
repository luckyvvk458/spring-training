package com.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    StudentDAO studentDAO;

    public void registerStudentDao(Student student) {
        studentDAO.save(student);
        System.out.println("Student Registered Successfully ...");
    }

    public List<Student> getAllStudents() {

        return studentDAO.findAll();
    }

    public void updateStudent(
            Student student) {
        studentDAO.update(student);
        System.out.println(
                "Student Updated Successfully");
    }
    public void deleteStudent(int id) {
        studentDAO.delete(id);
        System.out.println(
                "Student Deleted Successfully");
    }
}
