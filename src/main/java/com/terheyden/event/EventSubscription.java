package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedFunction1;

/**
 * EventSubscription class.
 */
public final class EventSubscription {

    private final UUID subscriptionId;
    private final CheckedFunction1<Object, Object> eventHandler;

    private EventSubscription(
        UUID subscriptionId,
        CheckedFunction1<Object, Object> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    public static EventSubscription createNew(CheckedFunction1<Object, Object> eventHandler) {
        return new EventSubscription(UUID.randomUUID(), eventHandler);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedFunction1<Object, Object> getEventHandler() {
        return eventHandler;
    }
}
