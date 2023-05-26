package com.terheyden.event;

import java.util.Collection;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
class PublishRequest<T> {

    private final EventRequest<T> eventRequest;
    private final SendEventStrategy<T> sendEventStrategy;
    private final Collection<EventSubscription> subscribers;

    PublishRequest(
        EventRequest<T> eventRequest,
        SendEventStrategy<T> sendEventStrategy,
        Collection<EventSubscription> subscribers) {

        this.eventRequest = eventRequest;
        this.sendEventStrategy = sendEventStrategy;
        this.subscribers = subscribers;
    }

    EventRequest<T> eventRequest() {
        return eventRequest;
    }

    SendEventStrategy<T> eventPublisher() {
        return sendEventStrategy;
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
