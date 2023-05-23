package com.terheyden.event;

import java.util.Collection;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
class PublishRequest<T> {

    private final EventRequest<T> eventRequest;
    private final SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy;
    private final Collection<EventSubscription> subscribers;

    PublishRequest(
        EventRequest<T> eventRequest,
        SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy,
        Collection<EventSubscription> subscribers) {

        this.eventRequest = eventRequest;
        this.sendEventToSubscriberStrategy = sendEventToSubscriberStrategy;
        this.subscribers = subscribers;
    }

    EventRequest<T> eventRequest() {
        return eventRequest;
    }

    SendEventToSubscriberStrategy<T> eventPublisher() {
        return sendEventToSubscriberStrategy;
    }

    Collection<EventSubscription> subscribers() {
        return subscribers;
    }

    @Override
    public String toString() {
        return String.format("PublishRequest [%s]; %d subs => %s",
            eventRequest(),
            subscribers().size(),
            eventPublisher().getClass().getSimpleName());
    }
}
