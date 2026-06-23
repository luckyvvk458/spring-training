package com.training;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


public class DieselEngine implements Engine{
    DieselEngine(){
        System.out.println("Diesel Engine Object is Created ...");
    }

    @Override
    public void run() {
        System.out.println("Diesel engine running..");
    }
}
