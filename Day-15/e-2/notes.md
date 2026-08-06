# Train Management Microservice

# Student Notes

## Requirement 2 - Ignore Trains Departing in the Next 30 Minutes

------------------------------------------------------------------------

# Learning Objective

After completing this chapter you will be able to:

-   Understand how business requirements evolve.
-   Modify an existing implementation instead of rewriting it.
-   Use `LocalDateTime.plusMinutes()`.
-   Create a time window using lower and upper boundaries.
-   Filter trains that depart after the next 30 minutes.

------------------------------------------------------------------------

# Original Requirement

> Trains departing in the next 30 minutes should be ignored.

------------------------------------------------------------------------

# What Changed?

In Requirement 1 we displayed trains between:

``` text
Current Time --------------------> Current Time + 12 Hours
```

Example:

``` text
09:00 AM ------------------------> 09:00 PM
```

Every train after 09:00 AM was included.

Now the assignment changes the business rule.

------------------------------------------------------------------------

# New Business Rule

Ignore trains leaving during the next 30 minutes.

Example:

``` text
Current Time = 09:00 AM
```

Window becomes

``` text
09:00 ---------09:30---------------------------21:00
   Ignore          Include
```

The lower boundary changes from:

``` java
currentTime
```

to

``` java
currentTime.plusMinutes(30)
```

------------------------------------------------------------------------

# Why?

Imagine a passenger opens the application at **09:00 AM**.

A train leaving at **09:10 AM** is practically impossible to catch.

Similarly,

-   09:15 ❌
-   09:20 ❌
-   09:29 ❌

These trains should not be displayed.

However,

-   09:30 ✅
-   09:31 ✅

should be displayed.

------------------------------------------------------------------------

# Sample Data

Assume:

``` text
Current Time = 09:00
```

Train                Departure   Result
  -------------------- ----------- ---------------------
Chennai Express      08:30       ❌ Already departed
Mysore Express       09:10       ❌ Ignore
Goa Express          09:20       ❌ Ignore
Coimbatore Express   09:29       ❌ Ignore
Kochi Express        09:30       ✅ Include
Madurai Express      09:31       ✅ Include
Bangalore Express    10:15       ✅ Include

------------------------------------------------------------------------

# Algorithm

1.  Read current time.
2.  Calculate allowed departure time.

``` java
currentTime.plusMinutes(30)
```

3.  Calculate end of the window.

``` java
currentTime.plusHours(12)
```

4.  Read all trains.
5.  Include only trains between the allowed departure time and the next
    12 hours.

------------------------------------------------------------------------

# Step 1

``` java
LocalDateTime currentTime =
        LocalDateTime.of(2026,8,6,9,0);

LocalDateTime allowedDepartureTime =
        currentTime.plusMinutes(30);

LocalDateTime next12Hours =
        currentTime.plusHours(12);
```

Result:

``` text
Current Time          : 09:00
Allowed Departure     : 09:30
End of Window         : 21:00
```

------------------------------------------------------------------------

# Service Implementation

``` java
public List<Train> getTrainsIgnoringNext30Minutes() {

    LocalDateTime currentTime =
            LocalDateTime.of(2026, 8, 6, 9, 0);

    LocalDateTime allowedDepartureTime =
            currentTime.plusMinutes(30);

    LocalDateTime next12Hours =
            currentTime.plusHours(12);

    return trainRepository.findAll()
            .stream()
            .filter(train -> {

                LocalDateTime departureTime =
                        train.getDepartureTime();

                boolean isAfter30Minutes =
                        !departureTime.isBefore(allowedDepartureTime);

                boolean isWithinNext12Hours =
                        !departureTime.isAfter(next12Hours);

                return isAfter30Minutes
                        && isWithinNext12Hours;

            })
            .toList();
}
```

------------------------------------------------------------------------

# Understanding the Conditions

## Lower Boundary

``` java
!departureTime.isBefore(allowedDepartureTime)
```

Meaning:

-   09:29 ❌
-   09:30 ✅
-   09:31 ✅

This is equivalent to **departureTime \>= allowedDepartureTime**.

## Upper Boundary

``` java
!departureTime.isAfter(next12Hours)
```

Meaning:

-   20:30 ✅
-   21:00 ✅
-   21:01 ❌

Equivalent to **departureTime \<= next12Hours**.

------------------------------------------------------------------------

# Controller

``` java
@GetMapping("/next12hours/ignore30minutes")
public List<Train> getTrainsIgnoringNext30Minutes() {
    return trainService.getTrainsIgnoringNext30Minutes();
}
```

------------------------------------------------------------------------

# Expected Output

``` json
[
  {
    "name":"Kochi Express",
    "departureTime":"2026-08-06T09:30:00"
  },
  {
    "name":"Madurai Express",
    "departureTime":"2026-08-06T09:31:00"
  },
  {
    "name":"Bangalore Express",
    "departureTime":"2026-08-06T10:15:00"
  }
]
```

------------------------------------------------------------------------

# Requirement 1 vs Requirement 2

  -----------------------------------------------------------------------
Requirement 1                       Requirement 2
  ----------------------------------- -----------------------------------
Lower boundary = currentTime        Lower boundary =
currentTime.plusMinutes(30)

Shows every future train            Ignores trains in the next 30
minutes

Simpler filter                      Enhanced business rule
-----------------------------------------------------------------------

Notice that **the database did not change**. Only the **business logic**
changed.

------------------------------------------------------------------------

# Time Complexity

-   Read all trains: O(n)
-   Filter: O(n)

Overall complexity: **O(n)**

------------------------------------------------------------------------

# Common Mistakes

-   Using `isAfter()` instead of `!isBefore()`, which excludes the 09:30
    boundary.
-   Forgetting to keep the 12-hour upper limit.
-   Comparing strings instead of `LocalDateTime`.
-   Rewriting the entire method instead of changing only the lower
    boundary.

------------------------------------------------------------------------

# Interview Questions

1.  Why use `plusMinutes(30)`?
2.  Why is `!isBefore()` preferred over `isAfter()` here?
3.  What happens to a train departing exactly at 09:30?
4.  Did this requirement require a database change?
5.  Which application layer changed?

------------------------------------------------------------------------

# Practice Exercises

1.  Ignore trains departing in the next 15 minutes.
2.  Ignore trains departing in the next 45 minutes.
3.  Make the ignore window configurable using a request parameter.
4.  Use `LocalDateTime.now()` instead of a fixed time for a production
    version.
