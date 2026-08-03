# Session 2 -- Spring AOP: @Around, Execution Time, Logging & API Analytics

## Objectives

-   Understand @Around advice
-   Learn ProceedingJoinPoint and proceed()
-   Measure execution time
-   Use SLF4J logging
-   Log exceptions
-   Build API analytics

## Revision

Previously covered: - Aspect - Advice - Join Point - Pointcut -
@Before - @After

Neither @Before nor @After can stop execution.

## Understanding @Around

Flow:

Client -\> Spring Proxy -\> @Around Advice -\> proceed() ? -\>
Controller

Without proceed(), controller never executes.

Example:

``` java
@Around("execution(* com.training.demo_train_service.controller.*.*(..))")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    System.out.println("Before");

    Object result = joinPoint.proceed();

    System.out.println("After");

    return result;
}
```

If proceed() is removed, the controller is never invoked.

## Measuring Execution Time

``` java
long start = System.currentTimeMillis();
Object result = joinPoint.proceed();
long end = System.currentTimeMillis();
System.out.println("Execution Time : " + (end-start) + " ms");
return result;
```

Add Thread.sleep(2000) in the controller to demonstrate timing.

currentTimeMillis() gives timestamps. nanoTime() is better for elapsed
time.

## SLF4J

``` java
private static final Logger logger =
LoggerFactory.getLogger(LoggingAspect.class);

logger.info("Controller started");
logger.warn("Train not found");
logger.error("Database connection failed");
```

Spring Boot prints logs to the local console by default.

## Exception Logging

``` java
try{
    return joinPoint.proceed();
}catch(Exception ex){
    logger.error("Exception : {}", ex.getMessage());
    throw ex;
}
```

## API Analytics

Total count:

``` java
private int count=0;

@Around("execution(* com.training.demo_train_service.controller.*.*(..))")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

    count++;
    System.out.println("Total API Calls : "+count);

    return joinPoint.proceed();
}
```

Why does count increase?

LoggingAspect is a Spring Singleton bean. The same object handles every
request, so the field retains its value.

Per API count:

``` java
private final Map<String,Integer> analytics = new HashMap<>();

@Around("execution(* com.training.demo_train_service.controller.*.*(..))")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    String api = joinPoint.getSignature().getName();

    analytics.put(api,
            analytics.getOrDefault(api,0)+1);

    System.out.println(analytics);

    return joinPoint.proceed();
}
```

Sample output:

    {getAllTrains=1}
    {getAllTrains=2}
    {getAllTrains=2, addTrain=1}
    {getAllTrains=2, addTrain=1, deleteTrain=1}

Production note: Prefer ConcurrentHashMap\<String, AtomicInteger\> for
thread safety.

## Assignment

Implement an aspect that logs: - Method Name - Start Time - End Time -
Execution Time - Success / Failure - API call count
