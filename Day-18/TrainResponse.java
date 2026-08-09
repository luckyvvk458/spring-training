package com.training.demo_train_service_client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TrainPricingResponse {

    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private Integer delayMinutes;
    private CoachType coachType;
    private Integer availableSeats;
    private Double price;
}
