package com.training;

import org.springframework.stereotype.Component;

@Component
public class Car {
    Car(){
        System.out.println("Car Object is Creted ...");
    }
    public void drive(){
        System.out.println("Driving car ...");
    }
}
