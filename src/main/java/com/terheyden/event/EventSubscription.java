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
        CheckedConsumer<Object> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    @SuppressWarnings("unchecked")
    public static <T> EventSubscription createNew(CheckedConsumer<T> eventHandler) {
        return new EventSubscription(UUID.randomUUID(), (CheckedConsumer<Object>) eventHandler);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedConsumer<Object> getEventHandler() {
        return eventHandler;
    }
}
