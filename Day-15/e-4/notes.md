# Train Management Microservice

# Student Notes

## Requirement 4 - Support Sleeper and AC Coach Pricing

------------------------------------------------------------------------

# Learning Objective

After completing this chapter you will be able to:

-   Understand why **price belongs to Coach** instead of Train.
-   Extend the normalized Train-Coach model.
-   Store different prices for different coach types.
-   Save parent and child entities using JPA.
-   Understand why no new API is required for this requirement.

------------------------------------------------------------------------

# Original Requirement

> Support Sleeper and AC coach pricing by storing price inside Coach
> instead of Train.

------------------------------------------------------------------------

# Understanding the Requirement

In our initial application, the Train entity contained:

``` java
private Double ticketPrice;
```

Question:

**Which ticket price is this?**

-   Sleeper?
-   AC?

We cannot answer.

A single train can have multiple coach types, and each coach has its own
price.

Example:

Train             Coach       Price
  ----------------- --------- -------
Chennai Express   Sleeper      ₹650
Chennai Express   AC          ₹1450

A single `ticketPrice` field cannot store both values.

------------------------------------------------------------------------

# Why Not Add Two Fields?

One possible solution is:

``` java
private Double sleeperPrice;
private Double acPrice;
```

Although it works today, it fails when new coach types are introduced.

Example:

-   Third AC
-   Chair Car
-   General

The Train entity keeps growing with new columns.

Instead, we normalize the model.

------------------------------------------------------------------------

# Normalized Model

## Train (Parent)

``` text
Train
----------------------------
trainNumber
trainName
source
destination
departureTime
delayMinutes
List<Coach>
```

## Coach (Child)

``` text
Coach
----------------------------
coachType
availableSeats
price
Train train
```

Relationship:

``` text
            Train
              |
              | 1
              |
              *
            Coach
```

One Train can have many Coaches.

Each Coach has:

-   Coach Type
-   Available Seats
-   Price

------------------------------------------------------------------------

# Entity Design

## Train Entity

``` java
@OneToMany(mappedBy = "train",
           cascade = CascadeType.ALL)
private List<Coach> coaches;
```

Train stores only common information.

------------------------------------------------------------------------

## Coach Entity

``` java
@Entity
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String coachType;

    private Integer availableSeats;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;
}
```

Notice that **price belongs to Coach**.

------------------------------------------------------------------------

# Sample JSON Request

``` json
{
  "trainName":"Bangalore Express",
  "source":"Hyderabad",
  "destination":"Bangalore",
  "departureTime":"2026-08-06T10:15:00",
  "delayMinutes":0,
  "coaches":[
    {
      "coachType":"SLEEPER",
      "availableSeats":120,
      "price":550
    },
    {
      "coachType":"AC",
      "availableSeats":40,
      "price":1250
    }
  ]
}
```

------------------------------------------------------------------------

# Saving Parent and Child

Before saving, establish the relationship.

``` java
for (Coach coach : train.getCoaches()) {
    coach.setTrain(train);
}

trainRepository.save(train);
```

## Why is this Required?

`Coach` owns the relationship.

``` java
@ManyToOne
@JoinColumn(name = "train_id")
private Train train;
```

Hibernate reads the `train` reference inside every Coach to populate the
foreign key (`train_id`).

Without calling:

``` java
coach.setTrain(train);
```

the relationship is incomplete.

------------------------------------------------------------------------

# Controller

``` java
@PostMapping("/addTrain")
public void addTrain(@RequestBody Train train) {
    trainsService.addTrain(train);
}
```

------------------------------------------------------------------------

# Service

``` java
public void addTrain(Train train) {

    for (Coach coach : train.getCoaches()) {
        coach.setTrain(train);
    }

    trainRepository.save(train);
}
```

Notice that the relationship logic belongs in the **Service Layer**, not
the Controller.

------------------------------------------------------------------------

# Does This Requirement Need a New API?

**No.**

The existing POST and GET APIs continue to work.

Only the domain model has improved.

------------------------------------------------------------------------

# Advantages

-   Price is stored in the correct entity.
-   Supports unlimited coach types.
-   No database changes when a new coach type is introduced.
-   Real-world railway design.

------------------------------------------------------------------------

# Common Mistakes

-   Keeping `ticketPrice` in Train.
-   Forgetting `coach.setTrain(train)`.
-   Saving Coach separately without linking it to Train.
-   Forgetting `CascadeType.ALL`.

------------------------------------------------------------------------

# Interview Questions

1.  Why does price belong to Coach?
2.  Why not store `sleeperPrice` and `acPrice` in Train?
3.  Which entity owns the relationship?
4.  Why is `@JoinColumn` present in Coach?
5.  Why do we call `coach.setTrain(train)`?

------------------------------------------------------------------------

# Practice Exercises

1.  Add a Third AC coach with a different price.
2.  Add a Chair Car coach.
3.  Calculate the minimum priced coach for a train.
4.  Display all coach prices for a train.

------------------------------------------------------------------------

# Summary

Requirement 4 is **not about creating a new API**.

It is about improving the **domain model** by moving `price` from Train
to Coach.

This normalized design prepares the application for the next
requirements:

-   Sort by Price
-   Sort by Seats
-   Sort by Departure Time
