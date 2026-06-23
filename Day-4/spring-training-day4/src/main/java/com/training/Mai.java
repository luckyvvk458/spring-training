package com.training;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Mai {
    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);
        StudentDAO studentDAO = context.getBean(StudentDAO.class);
        Student student = new Student(6, "andan6");
        studentDAO.saveStudnet(student);
    }
}
