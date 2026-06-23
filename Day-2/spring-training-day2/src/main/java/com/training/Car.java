package com.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Car {
    Car() {
        System.out.println("Car Object is created... ");
    }

    @Autowired
    @Lazy
    Engine engine;

    public void drive() {
        engine.run();
        System.out.println("Driving car ...");
    }
}
