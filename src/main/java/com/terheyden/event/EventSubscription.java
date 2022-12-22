package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * EventSubscription class.
 */
public final class EventSubscription {

    private final UUID subscriptionId;
    private final CheckedConsumer<Object> eventHandler;

    private EventSubscription(
        UUID subscriptionId,
        CheckedConsumer<?> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = recastConsumer(eventHandler);
    }

    public static EventSubscription createNew(UUID subscriptionId, CheckedConsumer<?> eventHandler) {
        return new EventSubscription(subscriptionId, eventHandler);
    }

    @SuppressWarnings("unchecked")
    private static CheckedConsumer<Object> recastConsumer(CheckedConsumer<?> eventHandler) {
        CheckedConsumer rawConsumer = eventHandler;
        return (CheckedConsumer<Object>) rawConsumer;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedConsumer<Object> getEventHandler() {
        return eventHandler;
    }
}
