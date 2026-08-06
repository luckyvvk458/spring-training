# Train Service Assignment Notes

## Overview

These notes summarize the implementation roadmap and solutions for
requirements 1-7.

### Requirement 1

Display all trains departing in the next 12 hours using LocalDateTime
filtering between currentTime and currentTime.plusHours(12).

### Requirement 2

Ignore trains departing in the next 30 minutes by changing the lower
boundary to currentTime.plusMinutes(30).

### Requirement 3

Support Sleeper and AC coach seat availability by normalizing the model
into Train (parent) and Coach (child).

### Requirement 4

Support Sleeper and AC coach pricing by storing price inside Coach
instead of Train.

### Normalized Model

-   Train -\> trainNumber, trainName, source, destination,
    departureTime, delayMinutes, List`<Coach>`{=html}
-   Coach -\> coachType, availableSeats, price, Train train

### Saving Parent and Child

Before save:

``` java
for(Coach coach: train.getCoaches()){
    coach.setTrain(train);
}
trainRepository.save(train);
```

### Requirement 5

Sort by coach price (Ascending).

### Requirement 6

If price is same, sort by coach availableSeats (Descending).

### Requirement 7

If price and seats are same, sort by departureTime (Descending).

Comparator order: 1. Price ASC 2. Seats DESC 3. Departure Time DESC

Remaining: - Delayed trains - Railway API integration - Authentication -
Performance
