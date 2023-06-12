# EventRouter

_Simple, fast, flexible event router / event bus for Java._

## What is it?
An extremely fast and lightweight event bus for Java libraries and applications.
Decouple your components by using event-driven design.

## Example Usage
```java
// Let's make a simple "Hello World" event router.
// This event says, "I have a printable string," and notifies all subscribers.
EventRouter<String> printableStringEvent = EventRouters
    .createWithEventType(String.class)
    .build();

// We'll make a simple subscriber that prints the string.
printableStringEvent.subscribe(str -> System.out.println("Received: " + str));

// Publish a String event to test.
printableStringEvent.publish("Hello, world!");
```

## EventRouter Features

* Tiny (50k)
* Fast — the default config will process around 2-4M events per second (see `EventRouterLoadTest.java`)
* Flexible without messing with your source code
    * No annotations required
    * No special interfaces or unique classes necessary
    * No reflection, no classpath scanning
* JDK 11+ compatible

## How to use

One simple dependency:

### Maven
```xml
<dependency>
    <groupId>com.terheyden</groupId>
    <artifactId>event-router</artifactId>
    <version>0.1.0</version>
</dependency>
```

## More example usages

```java
@Test
void tutorial2() {

    // You can also send events that return a response object.
    // We call this an "event query."
    // In this simple example, we'll publish a string event and expect the string length as a reply.
    EventQuery<String, Integer> stringLengthQuery = EventRouters
        .createWithEventType(String.class)
        .eventReplyType(Integer.class)
        .build();

    // Subscribe to the event, and calculate the string length as the response.
    stringLengthQuery.subscribe(String::length);

    // Publish a string event and specify the callback to call when the response is received.
    // Remember that publishing and subscribing always happens asynchronously.
    stringLengthQuery.query(
        "Hello, world!",
        strLen -> System.out.println("String length: " + strLen));

    EventUtils.sleep(200);
}

@Test
void tutorial3() {

    // The last kind of event you can send is a modifiable event.
    // Subscribers are given the event object in FIFO order,
    // and may update, replace, or event cancel the event by returning null.
    ModifiableEventRouter<String> userIdFoundEvent = EventRouters
        .createWithEventType(String.class)
        .modifiableEvents()
        .build();

    // The first subscriber ("governor") changes the ID to uppercase.
    userIdFoundEvent.subscribe(String::toUpperCase);

    // The second governor makes sure the ID has the proper length.
    // Setting the event obj to null will cancel the event from propagating further.
    userIdFoundEvent.subscribe(str -> str.length() == 5 ? str : null);

    // If it gets past all governors, then load the user ID...
    userIdFoundEvent.subscribeReadOnly(str -> System.out.println("Loading user ID: " + str));

    // Send a bad ID:
    userIdFoundEvent.publish("123");
    // Send a good ID:
    userIdFoundEvent.publish("12345");

    EventUtils.sleep(200);
}

@Test
void tutorial4() {

    // For the last tutorial, let's go over some advanced features.
    // Currently, each event router uses its own thread pool.
    // Depending on your use case, you may want to have many events share a single thread pool.
    ThreadPoolExecutor threadPool = ThreadPools.newDynamicThreadPool(1000);

    EventRouter<String> sharedPoolEventRouter1 = EventRouters
        .createWithEventType(String.class)
        .customThreadPool(threadPool)
        .build();

    EventRouter<String> sharedPoolEventRouter2 = EventRouters
        .createWithEventType(String.class)
        .customThreadPool(threadPool)
        .build();

    // If a subscriber throws an exception while handling an event, it is caught and logged
    // so that it doesn't affect other subscribers.
    // If you wish to use a custom exception handler, you can do so like this:
    EventRouter<UUID> userLoggedIn = EventRouters
        .createWithEventType(UUID.class)
        .exceptionHandler((event, ex) -> System.out.println("Exception: " + ex))
        .build();
}
```
