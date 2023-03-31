package com.terheyden.event;

import java.util.Queue;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
class PublishRequest {

    private final EventRouter eventRouter;
    private final Object eventObj;
    private final Class<?> eventType;
    private final SendEventToSubscriberStrategy sendEventToSubscriberStrategy;
    private final Queue<EventSubscription> subscribers;

    PublishRequest(
        EventRouter eventRouter,
        Object eventObj,
        Class<?> eventType,
        SendEventToSubscriberStrategy sendEventToSubscriberStrategy,
        Queue<EventSubscription> subscribers) {

        this.eventRouter = eventRouter;
        this.eventType = eventType;
        this.eventObj = eventObj;
        this.sendEventToSubscriberStrategy = sendEventToSubscriberStrategy;
        this.subscribers = subscribers;
    }

    Object event() {
        return eventObj;
    }

    Class<?> eventType() {
        return eventType;
    }

    SendEventToSubscriberStrategy eventPublisher() {
        return sendEventToSubscriberStrategy;
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
