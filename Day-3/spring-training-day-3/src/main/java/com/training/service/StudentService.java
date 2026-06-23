package com.training.service;

import com.training.repository.StudentRepository;
import com.training.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    NotificationService notificationService;

    public void registerStudent(Student student){
        studentRepository.save(student);
        notificationService.sendNotification(student.getName());
        System.out.println("Student registered successfully ...");
    }

}
