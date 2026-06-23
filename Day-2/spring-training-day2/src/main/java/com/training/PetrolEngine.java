package com.training;

import org.springframework.stereotype.Component;

@Component
public class PetrolEngine implements Engine{
    PetrolEngine(){
        System.out.println("Petrol Engine Object is created.....");
    }

    @Override
    public void run() {
        System.out.println("Petrol Engine is running ...");
    }
}
