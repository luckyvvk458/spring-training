package com.training;

import com.training.model.Course;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.training")
public class AppConfig {

    @Bean
    public Course getCourse(){
        return new Course("Spring Frame Work ");
    }

}
