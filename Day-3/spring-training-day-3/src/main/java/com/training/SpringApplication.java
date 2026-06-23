package com.training;

import com.training.model.Course;
import com.training.model.Student;
import com.training.service.StudentService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringApplication {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        StudentService studentService = context.getBean(StudentService.class);
        Course course = context.getBean(Course.class);
        Student student = new Student(1, "vivek");
        studentService.registerStudent(student);
        System.out.println("Courses offered "+course.getCourseName());
    }
}
