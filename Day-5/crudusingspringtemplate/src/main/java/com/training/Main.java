package com.training;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        StudentService studentService = context.getBean(StudentService.class);

//        System.out.println("Insert ");
//        studentService.registerStudentDao(new Student(3, "vvk"));
        System.out.println("Read ");
        studentService.getAllStudents().forEach(System.out::println);

//        System.out.println(" Update ");
//        studentService.updateStudent(
//                new Student(
//                        1,
//                        "Rahul Kumar"));
//        System.out.println();
//        System.out.println("Read after update ");
//        studentService.getAllStudents().forEach(System.out::println);
//
//        System.out.println();
//        System.out.println("Delete ");
//
//        studentService.deleteStudent(1);
//
//        System.out.println();
//
//        System.out.println("Read after Delete ");
//
//        studentService.getAllStudents().forEach(System.out::println);
        System.out.println("Hello world!");
    }
}