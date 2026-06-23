package com.training;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DieselEngine implements Engine {
    DieselEngine(){
        System.out.println("Diesel Engine Object is created ...");
    }

    @Override
    public void run() {
        System.out.println("Diesel Engine running ... ");
    }
}
