# Service Discovery with Eureka – Hands-On Notes

## 1. Objective

In a microservices application, one service often needs to call another service.

Our example contains:

- **Demo Train Service (John Doe Railway Service)** — port `8080`
- **Demo Train Service Client** — port `8081`
- **Eureka Server** — port `8761`

The objective is to remove the hardcoded service address:

```text
http://localhost:8080
```

and discover the Train Service dynamically by its registered Eureka name:

```text
DEMO-TRAIN-SERVICE
```

---

## 2. The Hardcoded URL Problem

Initially, the client was configured like this:

```java
public RailwayClient(RestClient.Builder builder) {
    this.restClient = builder
            .baseUrl("http://localhost:8080")
            .build();
}
```

Then:

```java
restClient.get()
        .uri("/trains/getAll")
```

produced the request:

```text
http://localhost:8080/trains/getAll
```

### Why is this a problem?

The client directly knows where the Train Service is running.

```text
Train Service Client
        |
        | hardcoded address
        v
http://localhost:8080
        |
        v
Train Service
```

If the Train Service moves to another machine or port, the client configuration must change.

With many services, this creates many service-to-service address dependencies.

---

## 3. Service Discovery

Instead of every service remembering every other service's physical address, we use a **Service Registry**.

Eureka acts as the registry.

```text
                 Eureka Server
                      |
              Registry Information
                 /             \
                /               \
               v                 v
DEMO-TRAIN-SERVICE      DEMO-TRAIN-SERVICE-CLIENT
      :8080                     :8081
```

Conceptually, Eureka stores mappings such as:

```text
DEMO-TRAIN-SERVICE
    -> DESKTOP-VFM5565:8080

DEMO-TRAIN-SERVICE-CLIENT
    -> DESKTOP-VFM5565:8081
```

The client can now ask for:

```text
DEMO-TRAIN-SERVICE
```

instead of permanently storing:

```text
localhost:8080
```

---

## 4. How Does the Client Know the Eureka Server Address?

This is an important question.

The Eureka Server itself must be running at a known address.

For our local application:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

So the client knows:

```text
http://localhost:8761/eureka/
```

The architecture is:

```text
Train Service Client
        |
        | knows Eureka address
        v
Eureka Server :8761
        |
        | provides registry information
        v
DEMO-TRAIN-SERVICE
        |
        v
current host + port
```

Service Discovery does not eliminate every configured address. The registry address is a bootstrap/infrastructure configuration.

The major benefit is that services no longer need to know the address of every other service.

---

## 5. What Happens If Eureka Goes Down?

Eureka is not normally in the path of every API request.

The request path is not:

```text
Client -> Eureka -> Train Service
```

Instead:

```text
Client -> Train Service
```

Eureka provides registry information. Eureka clients can maintain locally fetched registry information.

Therefore, if Eureka becomes unavailable after clients have already obtained service information, existing communication may continue using previously known registry data.

However:

- new services may not be able to register
- fresh registry updates may not be available
- changed service locations may eventually become stale

For higher availability, production environments can use multiple registry nodes.

---

## 6. Both Services Are Registered

The Eureka dashboard currently shows:

```text
DEMO-TRAIN-SERVICE
UP

DEMO-TRAIN-SERVICE-CLIENT
UP
```

This means registration is successful.

Current architecture:

```text
                    +----------------------+
                    | Eureka Server :8761  |
                    +----------+-----------+
                               |
                 +-------------+-------------+
                 |                           |
                 v                           v
      DEMO-TRAIN-SERVICE        DEMO-TRAIN-SERVICE-CLIENT
            :8080                         :8081
```

The next step is to use this registration information for actual service discovery.

---

## 7. Manual Discovery with DiscoveryClient

For the current learning step, we are intentionally not using load balancing yet.

We use:

```java
org.springframework.cloud.client.discovery.DiscoveryClient
```

and:

```java
org.springframework.cloud.client.ServiceInstance
```

The flow is:

```text
RailwayClient
      |
      | getInstances("DEMO-TRAIN-SERVICE")
      v
DiscoveryClient
      |
      v
Service registry information
      |
      v
ServiceInstance
      |
      v
http://DESKTOP-VFM5565:8080
      |
      v
RestClient calls the Train Service
```

This helps us understand what happens before introducing `@LoadBalanced`.

---

## 8. Inject DiscoveryClient

Required imports:

```java
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
```

Add these fields:

```java
private final RestClient restClient;
private final DiscoveryClient discoveryClient;
```

Constructor:

```java
public RailwayClient(RestClient.Builder builder,
                     DiscoveryClient discoveryClient) {

    this.restClient = builder.build();
    this.discoveryClient = discoveryClient;
}
```

Notice that we removed:

```java
.baseUrl("http://localhost:8080")
```

The `RestClient` no longer permanently knows the Train Service address.

---

## 9. Discovering DEMO-TRAIN-SERVICE

We can ask for registered instances:

```java
ServiceInstance instance = discoveryClient
        .getInstances("DEMO-TRAIN-SERVICE")
        .get(0);
```

Then obtain the URI:

```java
String baseUrl = instance.getUri().toString();
```

For example:

```text
http://DESKTOP-VFM5565:8080
```

For debugging:

```java
System.out.println("Discovered Train Service: " + baseUrl);
```

Example output:

```text
Discovered Train Service: http://DESKTOP-VFM5565:8080
```

---

## 10. Reuse Discovery Logic

Both API methods need the Train Service location.

Instead of duplicating:

```java
discoveryClient.getInstances("DEMO-TRAIN-SERVICE").get(0);
```

create one helper method:

```java
private String getTrainServiceBaseUrl() {

    ServiceInstance instance = discoveryClient
            .getInstances("DEMO-TRAIN-SERVICE")
            .get(0);

    String baseUrl = instance.getUri().toString();

    System.out.println("Discovered Train Service: " + baseUrl);

    return baseUrl;
}
```

Now:

```text
findAllTrains()
      |
      v
getTrainServiceBaseUrl()

getTrainPricing()
      |
      v
getTrainServiceBaseUrl()
```

---

## 11. Calling the getAll Trains API

The discovered base URL may be:

```text
http://DESKTOP-VFM5565:8080
```

The endpoint is:

```text
/trains/getAll
```

The complete request URL becomes:

```text
http://DESKTOP-VFM5565:8080/trains/getAll
```

Method:

```java
@Retry(name = "johnDoe")
@Cacheable("allTrains")
public List<Train> findAllTrains() {

    System.out.println("Calling John Doe...");

    String baseUrl = getTrainServiceBaseUrl();

    return restClient.get()
            .uri(baseUrl + "/trains/getAll")
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<Train>>() {
            });
}
```

### Flow

```text
Client Request
      |
      v
Cache lookup
      |
      +--> Cache HIT -> return cached data
      |
      +--> Cache MISS
               |
               v
          Execute method
               |
               v
          Discover service
               |
               v
        DEMO-TRAIN-SERVICE
               |
               v
       Actual host + port
               |
               v
         RestClient call
               |
               v
          Receive response
               |
               v
          Store in cache
```

---

## 12. Calling the Pricing API

The endpoint is:

```text
/trains/sort-by-price
```

with:

```text
coachType
```

A clean approach is to use `UriComponentsBuilder`.

Import:

```java
import org.springframework.web.util.UriComponentsBuilder;
```

Method:

```java
public List<TrainPricingResponse> getTrainPricing(
        CoachType coachType) {

    String baseUrl = getTrainServiceBaseUrl();

    String url = UriComponentsBuilder
            .fromUriString(baseUrl)
            .path("/trains/sort-by-price")
            .queryParam("coachType", coachType)
            .toUriString();

    return restClient.get()
            .uri(url)
            .headers(headers ->
                    headers.setBasicAuth("admin", "admin123"))
            .retrieve()
            .body(new ParameterizedTypeReference<List<TrainPricingResponse>>() {
            });
}
```

Example:

```text
baseUrl:
http://DESKTOP-VFM5565:8080

coachType:
AC
```

Final URL:

```text
http://DESKTOP-VFM5565:8080/trains/sort-by-price?coachType=AC
```

---

## 13. Complete RailwayClient

```java
package com.training.demo_train_service_client.client;

import com.training.demo_train_service_client.dto.CoachType;
import com.training.demo_train_service_client.dto.Train;
import com.training.demo_train_service_client.dto.TrainPricingResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class RailwayClient {

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public RailwayClient(RestClient.Builder builder,
                         DiscoveryClient discoveryClient) {

        this.restClient = builder.build();
        this.discoveryClient = discoveryClient;
    }

    @Retry(name = "johnDoe")
    @Cacheable("allTrains")
    public List<Train> findAllTrains() {

        System.out.println("Calling John Doe...");

        String baseUrl = getTrainServiceBaseUrl();

        return restClient.get()
                .uri(baseUrl + "/trains/getAll")
                .headers(headers ->
                        headers.setBasicAuth("admin", "admin123"))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Train>>() {
                });
    }

    public List<TrainPricingResponse> getTrainPricing(
            CoachType coachType) {

        String baseUrl = getTrainServiceBaseUrl();

        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/trains/sort-by-price")
                .queryParam("coachType", coachType)
                .toUriString();

        return restClient.get()
                .uri(url)
                .headers(headers ->
                        headers.setBasicAuth("admin", "admin123"))
                .retrieve()
                .body(new ParameterizedTypeReference<List<TrainPricingResponse>>() {
                });
    }

    private String getTrainServiceBaseUrl() {

        ServiceInstance instance = discoveryClient
                .getInstances("DEMO-TRAIN-SERVICE")
                .get(0);

        String baseUrl = instance.getUri().toString();

        System.out.println("Discovered Train Service: " + baseUrl);

        return baseUrl;
    }
}
```

---

## 14. Cache, Retry and Service Discovery

These solve different problems.

### `@Cacheable`

```text
Avoids unnecessary remote calls when cached data is available.
```

### `@Retry`

```text
Retries temporary failures according to the configured retry policy.
```

### `DiscoveryClient`

```text
Finds the current registered location of a service.
```

### `RestClient`

```text
Makes the actual HTTP request.
```

### Eureka

```text
Maintains service registration and discovery information.
```

Conceptually:

```text
@Cacheable
    |
    +--> Cache HIT -> return data
    |
    +--> Cache MISS
             |
             v
           @Retry
             |
             v
      DiscoveryClient
             |
             v
      Eureka registry data
             |
             v
       ServiceInstance URI
             |
             v
         RestClient
             |
             v
       Remote service call
```

---

## 15. Why `.get(0)` Is Only the First Step

Currently:

```java
.getInstances("DEMO-TRAIN-SERVICE").get(0)
```

is useful for understanding manual discovery.

Later, suppose Eureka contains:

```text
DEMO-TRAIN-SERVICE

Instance 1 -> :8080
Instance 2 -> :8082
Instance 3 -> :8083
```

Then the discovery client can return multiple instances.

If we always use:

```java
.get(0)
```

we are manually selecting the first instance.

That is not load balancing.

This leads naturally to the next topic:

```text
Service Discovery
      |
      v
Multiple Instances
      |
      v
Which instance should receive the request?
      |
      v
Load Balancing
      |
      v
Spring Cloud LoadBalancer
      |
      v
@LoadBalanced
```

---

## 16. Learning Progress

```text
Hardcoded URL problem
        |
        v
Why hardcoded service locations are difficult
        |
        v
Service Registry
        |
        v
Eureka Server
        |
        v
Train Service registration
        |
        v
Train Service Client registration
        |
        v
Both services are UP
        |
        v
DiscoveryClient
        |
        v
Discover DEMO-TRAIN-SERVICE
        |
        v
Get ServiceInstance
        |
        v
Use discovered URI in RestClient
        |
        v
Next: Multiple instances and Load Balancing
```

---

## 17. Key Takeaways

1. Hardcoding `localhost:8080` creates a dependency on a fixed service location.
2. Eureka provides a central registry of service names and their current locations.
3. The Eureka Server itself must have a known bootstrap address.
4. Both services must register successfully before discovery can work.
5. `DiscoveryClient` can retrieve instances using the registered service name.
6. `ServiceInstance#getUri()` provides the actual URI of an instance.
7. The hardcoded Train Service base URL can be removed from `RestClient`.
8. `@Cacheable`, `@Retry`, Service Discovery, and `RestClient` have separate responsibilities.
9. `.get(0)` is useful for learning but is not a complete load-balancing solution.
10. The next logical topic is multiple instances and Spring Cloud LoadBalancer.
