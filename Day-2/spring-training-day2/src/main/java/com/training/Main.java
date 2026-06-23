package com.training;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        Car car = applicationContext.getBean(Car.class);
        Car car1 = applicationContext.getBean(Car.class);
        Car car2 = applicationContext.getBean(Car.class);
        car.drive();
        car1.drive();
        System.out.println(car);
        System.out.println(car1);
        System.out.println(car2);
        if(car == car1){
            System.out.println("Both Objects are equals ...");
        }else{
            System.out.println("Not equal ");
        }

    }
}