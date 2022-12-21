# EventRouter

_Simple, fast, flexible event router / event bus for Java._

## What is it?
Coordinating components and services via events, also known as
event-driven, publish-subscribe, message-driven, event-based,
or [the reactor pattern](https://en.wikipedia.org/wiki/Reactor_pattern),
is an important architectural pattern for building loosely coupled
applications.

EventRouter is a Java library that provides event-based messaging
in a lightweight, fast, and highly customizable way.

```java
// Subscribe to an event — events are differentiated by class:
eventRouter.subscribe(UserLoginEvent.class, this::handleUserLogin);

// Send an event:
eventRouter.publish(userLoginEvent);
```

## Why use it?
Coordinating components, services, and microservices by means of event routing
is the core of good software design (see Ports and Adapters, Onion Architecture, DDD)
and the cornerstone of cloud computing.

### EventRouter features

* Tiny (6k), powerful, and easy to use

* Fully custom thread support
  * Use our finely-tuned threadpools, or go ahead and supply your own `ExecutorService`

* No annotations required
  * No reflection, no classpath scanning, and less source code clutter
  * Subscribers can self-register during construction
  * Subscriptions are simply lambda functions (closures)

* JDK 1.8+ compatible
  * If you're stuck using JDK 1.8, we've got you covered

## TODO
More stuff goes here.

## Exception handling
If an exception is thrown while an event is being handled by a subscriber,
the exception and event details are wrapped in an `EventRouterPublishException`
and published as a separate event. That way, the caller can handle the
exception however they want.
