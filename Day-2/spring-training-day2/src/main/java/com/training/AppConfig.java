package com.training;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.training")
public class AppConfig {
    @Bean
    public Engine engine(){
        return new ElectricEngine();
    }
}
