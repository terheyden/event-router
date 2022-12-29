package com.terheyden.event;

import java.util.Queue;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
class PublishRequest {

    private final EventRouter eventRouter;
    private final Object eventObj;
    private final Class<?> eventType;
    private final EventPublisher eventPublisher;
    private final Queue<EventSubscription> subscribers;

    PublishRequest(
        EventRouter eventRouter,
        Object eventObj,
        Class<?> eventType,
        EventPublisher eventPublisher,
        Queue<EventSubscription> subscribers) {

        this.eventRouter = eventRouter;
        this.eventType = eventType;
        this.eventObj = eventObj;
        this.eventPublisher = eventPublisher;
        this.subscribers = subscribers;
    }

    Object event() {
        return eventObj;
    }

    Class<?> eventType() {
        return eventType;
    }

    EventPublisher eventPublisher() {
        return eventPublisher;
    }

    Queue<EventSubscription> subscribers() {
        return subscribers;
    }

    EventRouter eventRouter() {
        return eventRouter;
    }

    @Override
    public String toString() {
        return String.format("PublishRequest [%s (%s)]; %d subs => %s",
            eventType(),
            event(),
            subscribers().size(),
            eventPublisher().getClass().getSimpleName());
    }
}
