# Train Management Microservice

# Student Notes

## Requirement 5 - Sort Trains by Coach Price (Ascending)

------------------------------------------------------------------------

# Learning Objective

After completing this chapter you will be able to:

-   Understand the business requirement.
-   Sort trains based on the selected coach type.
-   Use Java Stream `sorted()`.
-   Compare prices in ascending order.
-   Build the foundation for multi-level sorting.

------------------------------------------------------------------------

# Original Requirement

> Trains should be displayed in ascending order of **price**.

------------------------------------------------------------------------

# Requirement Analysis

Since our application is normalized, **price is no longer stored in
Train**.

    Train
       |
       +----> Coach (Sleeper) -> Price
       |
       +----> Coach (AC)       -> Price

Question:

**Which price should we sort?**

-   Sleeper Price?
-   AC Price?

Therefore, the client must specify the coach type.

Example:

``` http
GET /trains/next12hours?coachType=SLEEPER
```

or

``` http
GET /trains/next12hours?coachType=AC
```

------------------------------------------------------------------------

# Sample Data

Train               Coach       Price
  ------------------- --------- -------
Bangalore Express   Sleeper       550
Hyderabad Express   Sleeper       500
Mumbai Express      Sleeper       700
Pune Express        Sleeper       480

Expected order:

1.  Pune Express (480)
2.  Hyderabad Express (500)
3.  Bangalore Express (550)
4.  Mumbai Express (700)

------------------------------------------------------------------------

# Algorithm

1.  Filter trains in the required time window.
2.  Find the requested coach inside each train.
3.  Compare coach prices.
4.  Return trains sorted in ascending order.

```{=html}
<!-- -->
```
    Filter
       ↓
    Locate Coach
       ↓
    Compare Price
       ↓
    Sort ASC

------------------------------------------------------------------------

# Helper Method

``` java
private Coach getRequestedCoach(Train train, String coachType){

    return train.getCoaches()
            .stream()
            .filter(coach ->
                    coach.getCoachType()
                         .equalsIgnoreCase(coachType))
            .findFirst()
            .orElse(null);
}
```

------------------------------------------------------------------------

# Sorting Logic

``` java
.sorted((train1, train2) -> {

    Coach coach1 = getRequestedCoach(train1, coachType);
    Coach coach2 = getRequestedCoach(train2, coachType);

    return Double.compare(
            coach1.getPrice(),
            coach2.getPrice());

})
```

`Double.compare()` returns:

-   Negative → train1 comes first
-   Zero → prices are equal
-   Positive → train2 comes first

Hence prices are sorted from **lowest to highest**.

------------------------------------------------------------------------

# Complete Flow

``` text
Client
   |
GET /trains/next12hours?coachType=SLEEPER
   |
Controller
   |
Service
   |
Find Requested Coach
   |
Compare Price
   |
Sorted List
```

------------------------------------------------------------------------

# Example Output

``` json
[
  {
    "trainName":"Pune Express"
  },
  {
    "trainName":"Hyderabad Express"
  },
  {
    "trainName":"Bangalore Express"
  },
  {
    "trainName":"Mumbai Express"
  }
]
```

------------------------------------------------------------------------

# Time Complexity

Finding the coach for each train:

-   n trains
-   m coaches per train

Complexity:

    O(n × m)

Since each train usually has only a few coach types, this is acceptable.

------------------------------------------------------------------------

# Common Mistakes

-   Sorting Train instead of Coach price.
-   Forgetting to filter by coach type.
-   Using `>` and `<` instead of `Double.compare()`.
-   Returning descending order accidentally.

------------------------------------------------------------------------

# Interview Questions

1.  Why can't Train be sorted directly by price?
2.  Why is coachType required?
3.  Why use `Double.compare()`?
4.  What is the time complexity?
5.  How does normalization affect sorting?

------------------------------------------------------------------------

# Practice Exercises

1.  Sort AC coaches by price.
2.  Handle invalid coach types.
3.  Return an empty list when the requested coach doesn't exist.
4.  Refactor the comparator using `Comparator.comparing()`.

------------------------------------------------------------------------

# Summary

Requirement 5 introduces the first business sorting rule.

Instead of reading a price from Train, we first locate the requested
Coach and then compare its price. This design works for Sleeper, AC,
Third AC, Chair Car, or any future coach type without changing the Train
entity.
