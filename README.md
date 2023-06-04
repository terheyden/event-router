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

* Tiny (6k)
* Fast — the default config will process over 2M events per second (see `EventRouterLoadTest.java`)
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
    <version>0.0.1</version>
</dependency>
```

# TODO
* exception handling
* publish metadata?
* Gradle
