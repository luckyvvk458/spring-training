# Train Service Project Notes (Requirements 5--7)

## Goal

After normalizing the model:

-   A Train contains multiple Coaches.
-   Each Coach has its own:
    -   coachType
    -   availableSeats
    -   price

Because price now belongs to Coach instead of Train, sorting must be
performed based on the selected coach type.

------------------------------------------------------------------------

# Entity Model

## Train

``` java
@OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
private List<Coach> coaches;
```

## Coach

``` java
@ManyToOne
@JoinColumn(name = "train_id")
private Train train;
```

This is a bidirectional relationship.

------------------------------------------------------------------------

# Infinite Recursion Problem

Returning JPA entities directly produced:

Train -\> Coach -\> Train -\> Coach -\> ...

Temporary fix:

``` java
// Train
@JsonManagedReference
private List<Coach> coaches;

// Coach
@JsonBackReference
private Train train;
```

Preferred solution: Return DTOs.

------------------------------------------------------------------------

# TrainResponse DTO

Fields

-   trainNumber
-   trainName
-   source
-   destination
-   departureTime
-   delayMinutes
-   coachType
-   availableSeats
-   price

Using Lombok:

``` java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainResponse { }
```

------------------------------------------------------------------------

# API

    GET /trains/sort-by-price?coachType=SLEEPER
    GET /trains/sort-by-price?coachType=AC

Controller

``` java
@GetMapping("/sort-by-price")
public List<TrainResponse> getTrainsSortedByPrice(
        @RequestParam CoachType coachType){

    return trainsService.getTrainsSortedByPrice(coachType);
}
```

------------------------------------------------------------------------

# Repository Query

Requirement 5

``` java
@Query("""
SELECT t
FROM Train t
JOIN t.coaches c
WHERE c.coachType=:coachType
ORDER BY c.price ASC
""")
```

Requirement 6

``` sql
ORDER BY
c.price ASC,
c.availableSeats DESC
```

Requirement 7

``` sql
ORDER BY
c.price ASC,
c.availableSeats DESC,
t.departureTime DESC
```

------------------------------------------------------------------------

# Service Flow

1.  Repository returns List`<Train>`{=html}
2.  Service converts Train -\> TrainResponse
3.  Controller returns List`<TrainResponse>`{=html}

Typical implementation

``` java
return trainRepo.findByCoachTypeOrderByPrice(coachType)
        .stream()
        .map(train -> buildTrainResponse(train, coachType))
        .toList();
```

Builder

``` java
return TrainResponse.builder()
        .trainNumber(train.getTrainNumber())
        .trainName(train.getTrainName())
        .source(train.getSource())
        .destination(train.getDestination())
        .departureTime(train.getDepartureTime())
        .delayMinutes(train.getDelayMinutes())
        .coachType(coach.getCoachType())
        .availableSeats(coach.getAvailableSeats())
        .price(coach.getPrice())
        .build();
```

------------------------------------------------------------------------

# How SQL Sorting Works

Priority 1

Price ASC

↓

Priority 2

Available Seats DESC

↓

Priority 3

Departure Time DESC

The next level is considered only when the previous level is tied.

------------------------------------------------------------------------

# Test Data Strategy

Requirement 5

Different prices.

Requirement 6

Same price + different seats.

Requirement 7

Same price + same seats + different departure times.

------------------------------------------------------------------------

# What We Learned

-   Normalize child-specific attributes into child entities.
-   Use DTOs instead of exposing JPA entities.
-   JPQL uses Java field names.
-   Keep sorting inside the repository whenever possible.
-   Service converts entities to DTOs.
-   Controller should expose DTOs, not entities.

------------------------------------------------------------------------

# Next Requirement

Requirement 8 introduces:

effectiveDepartureTime = departureTime + delayMinutes

Filtering must be based on effective departure time instead of only
departure time.
