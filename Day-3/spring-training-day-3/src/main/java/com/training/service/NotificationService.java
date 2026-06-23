package com.training.service;

import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    public void sendNotification(String name){
        System.out.println("Notification sent to "+name);
    }

}
