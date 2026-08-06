# Train Management Microservice

# Student Notes

## Requirement 1 - Display All Trains Departing in the Next 12 Hours

------------------------------------------------------------------------

# Learning Objective

By the end of this chapter you will be able to:

-   Understand the business requirement.
-   Convert the requirement into business logic.
-   Use `LocalDateTime`.
-   Filter data using Java Streams.
-   Build a REST API to return trains departing in the next 12 hours.

------------------------------------------------------------------------

# Original Requirement

> Display all trains departing in the next 12 hours.

------------------------------------------------------------------------

# Step 1 - Understand the Requirement

Suppose the current time is:

``` text
2026-08-06 09:00 AM
```

We should display trains departing between:

``` text
09:00 AM
      │
      ├────────────────────────────► 09:00 PM
              Next 12 Hours
```

Any train before 09:00 AM has already departed.

Any train after 09:00 PM should not be displayed.

------------------------------------------------------------------------

# Sample Data

Train               Departure          Include?
  ------------------- ------------------ ----------
Chennai Express     08:30              ❌
Bangalore Express   10:15              ✅
Hyderabad Express   12:45              ✅
Delhi Express       16:00              ✅
Pune Express        20:15              ✅
Kolkata Express     06:30 (Next Day)   ❌

------------------------------------------------------------------------

# Step 2 - Algorithm

1.  Read current time.
2.  Calculate currentTime + 12 hours.
3.  Read all trains.
4.  Keep only trains whose departure time lies within the window.
5.  Return the filtered list.

------------------------------------------------------------------------

# Step 3 - Why LocalDateTime?

We need both date and time.

Examples:

``` java
LocalDate
```

Contains only date.

``` java
LocalTime
```

Contains only time.

``` java
LocalDateTime
```

Contains both.

Hence we use:

``` java
LocalDateTime currentTime =
        LocalDateTime.of(2026,8,6,9,0);
```

For teaching we use a fixed time so the output remains the same every
day.

------------------------------------------------------------------------

# Step 4 - Calculate the End of the Window

``` java
LocalDateTime next12Hours =
        currentTime.plusHours(12);
```

Result

``` text
Current Time : 2026-08-06 09:00

Next Window : 2026-08-06 21:00
```

------------------------------------------------------------------------

# Step 5 - Service Implementation

``` java
public List<Train> getTrainsNext12Hours() {

    LocalDateTime currentTime =
            LocalDateTime.of(2026,8,6,9,0);

    LocalDateTime next12Hours =
            currentTime.plusHours(12);

    return trainRepository.findAll()
            .stream()
            .filter(train -> {

                LocalDateTime departureTime =
                        train.getDepartureTime();

                boolean isAfterCurrentTime =
                        !departureTime.isBefore(currentTime);

                boolean isBeforeNext12Hours =
                        !departureTime.isAfter(next12Hours);

                return isAfterCurrentTime
                        && isBeforeNext12Hours;

            })
            .toList();
}
```

------------------------------------------------------------------------

# Explanation

`isBefore()` returns true when departure is earlier.

Therefore:

``` java
!departureTime.isBefore(currentTime)
```

means

> departure time is equal to or after current time.

Similarly,

``` java
!departureTime.isAfter(next12Hours)
```

means

> departure time is equal to or before the end of the window.

------------------------------------------------------------------------

# Controller

``` java
@GetMapping("/next12hours")
public List<Train> getNext12Hours(){

    return trainService.getTrainsNext12Hours();

}
```

------------------------------------------------------------------------

# Expected Output

``` json
[
  {
    "name":"Bangalore Express",
    "departureTime":"2026-08-06T10:15:00"
  },
  {
    "name":"Hyderabad Express",
    "departureTime":"2026-08-06T12:45:00"
  }
]
```

------------------------------------------------------------------------

# Time Complexity

-   Reading data: O(n)
-   Filtering: O(n)

Overall: **O(n)**

------------------------------------------------------------------------

# Common Mistakes

-   Using `LocalDate` instead of `LocalDateTime`.
-   Comparing strings instead of dates.
-   Forgetting `plusHours(12)`.
-   Using `isAfter()` alone and excluding boundary values.

------------------------------------------------------------------------

# Interview Questions

1.  Why is `LocalDateTime` used?
2.  Why use `plusHours(12)`?
3.  Difference between `isBefore()` and `isAfter()`?
4.  Why use `!isBefore()` instead of `isAfter()`?
5.  What is the time complexity?

------------------------------------------------------------------------

# Practice

1.  Display trains departing in the next 6 hours.
2.  Display trains departing in the next 24 hours.
3.  Display trains between two custom times.
