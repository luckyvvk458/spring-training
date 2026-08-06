# Train Management Microservice

# Student Notes

## Requirement 3 - Support Sleeper and AC Coach Seat Availability

------------------------------------------------------------------------

# Learning Objective

After completing this chapter you will be able to:

-   Understand why the existing Train model is insufficient.
-   Learn database normalization using a real project.
-   Design a Parent-Child relationship using JPA.
-   Implement Train (Parent) and Coach (Child).
-   Save parent and child records together.

------------------------------------------------------------------------

# Original Requirement

> Support Sleeper and AC coach seat availability.

**Important:** This requirement does **not** introduce a new REST API.

Instead, it requires us to **improve our data model** so that it can
represent multiple coach types.

------------------------------------------------------------------------

# Existing Design

Initially our Train entity looked like this:

``` java
public class Train {

    private Integer id;
    private String name;
    private String source;
    private String destination;
    private LocalDateTime departureTime;

    private Integer availableSeats;
}
```

Question:

> Are these Sleeper seats or AC seats?

Answer:

We don't know.

The model cannot distinguish between different coach types.

------------------------------------------------------------------------

# Why the Existing Design Fails

Suppose Bangalore Express has

Coach       Seats
  --------- -------
Sleeper       120
AC             40

Can we store this using

``` java
private Integer availableSeats;
```

No.

We can store only one value.

This means our model is **not scalable**.

------------------------------------------------------------------------

# Naive Solution

Many beginners try this:

``` java
private Integer sleeperAvailableSeats;
private Integer acAvailableSeats;
```

It works for two coach types.

But tomorrow, if the railway introduces

-   Third AC
-   Chair Car
-   General

the Train entity keeps growing.

This violates good database design.

------------------------------------------------------------------------

# Normalization

Instead of storing coach information inside Train, we create a separate
Coach entity.

Relationship:

``` text
               1
Train ---------------------- Coach
               |
               |
               *
```

One Train

↓

Many Coaches

------------------------------------------------------------------------

# Database Design

## Train Table

Column
----------------
id
name
source
destination
departure_time

## Coach Table

Column
-----------------
id
coach_type
available_seats
price
train_id

Notice:

Seat availability belongs to Coach.

Not Train.

------------------------------------------------------------------------

# Entity Design

## Train

``` java
@Entity
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String source;

    private String destination;

    private LocalDateTime departureTime;

    @OneToMany(mappedBy = "train",
               cascade = CascadeType.ALL)
    private List<Coach> coaches;

}
```

------------------------------------------------------------------------

## Coach

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

------------------------------------------------------------------------

# Why Parent-Child?

Train stores common information.

Coach stores coach-specific information.

Example

``` text
Bangalore Express

↓

Sleeper
   Seats = 120

↓

AC
   Seats = 40
```

Tomorrow we can add another coach without changing the Train table.

------------------------------------------------------------------------

# Sample JSON

``` json
{
  "name":"Bangalore Express",
  "source":"Hyderabad",
  "destination":"Bangalore",
  "departureTime":"2026-08-06T10:15:00",
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
for(Coach coach : train.getCoaches()){
    coach.setTrain(train);
}

trainRepository.save(train);
```

Why?

Because Coach owns the foreign key.

``` java
@ManyToOne
@JoinColumn(name="train_id")
private Train train;
```

Without setting the parent reference, Hibernate cannot populate
`train_id`.

------------------------------------------------------------------------

# CRUD Impact

Controller: - No major change

Repository: - No change

Service: - Only establish parent-child relationship before saving.

Database: - Two tables instead of one.

------------------------------------------------------------------------

# Advantages of Normalization

-   No duplicate columns
-   Easy to add new coach types
-   Better database design
-   Real-world modelling
-   Supports future enhancements

------------------------------------------------------------------------

# Common Mistakes

-   Forgetting `coach.setTrain(train)`
-   Missing `cascade = CascadeType.ALL`
-   Using `mappedBy` incorrectly
-   Trying to store all coach information inside Train

------------------------------------------------------------------------

# Interview Questions

1.  Why normalize Train and Coach?
2.  Why is Train the parent?
3.  Why is Coach the owning side?
4.  Why is `@JoinColumn` placed in Coach?
5.  Why do we call `coach.setTrain(train)`?
6.  What happens if `CascadeType.ALL` is removed?

------------------------------------------------------------------------

# Practice Exercises

1.  Add a Third AC coach.
2.  Add a Chair Car coach.
3.  Display all coaches for a train.
4.  Count the total number of coaches in a train.

------------------------------------------------------------------------

# Summary

Requirement 3 is not about adding a new API.

It is about improving the domain model.

By normalizing the design into **Train (Parent)** and **Coach (Child)**,
the application becomes flexible, scalable, and closer to a real railway
reservation system.
