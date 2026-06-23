package com.training;

import org.springframework.stereotype.Component;


public class ElectricEngine implements Engine{
    ElectricEngine(){
        System.out.println("Electric Engine Object is created...");
    }
    @Override
    public void run() {
        System.out.println("Electric Engine is running...");
    }
}
