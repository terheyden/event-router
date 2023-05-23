package com.terheyden.event;

import java.util.Collection;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
class PublishRequest<T> {

    private final EventRouter<T> eventRouter;
    private final T eventObj;
    private final SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy;
    private final Collection<EventSubscription<T>> subscribers;

    PublishRequest(
        EventRouter<T> eventRouter,
        T eventObj,
        SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy,
        Collection<EventSubscription<T>> subscribers) {

        this.eventRouter = eventRouter;
        this.eventObj = eventObj;
        this.sendEventToSubscriberStrategy = sendEventToSubscriberStrategy;
        this.subscribers = subscribers;
    }

    T event() {
        return eventObj;
    }

    SendEventToSubscriberStrategy<T> eventPublisher() {
        return sendEventToSubscriberStrategy;
    }

    Collection<EventSubscription<T>> subscribers() {
        return subscribers;
    }

    EventRouter<T> eventRouter() {
        return eventRouter;
    }

    @Override
    public String toString() {
        return String.format("PublishRequest [%s (%s)]; %d subs => %s",
            event(),
            subscribers().size(),
            eventPublisher().getClass().getSimpleName());
    }
}
