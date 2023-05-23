package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * Used by {@link EventSubscriberMap} to store a subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class EventSubscription<T> {

    private final UUID subscriptionId;
    private final CheckedConsumer<T> eventHandler;

    EventSubscription(
        UUID subscriptionId,
        CheckedConsumer<T> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    EventSubscription(CheckedConsumer<T> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedConsumer<T> getEventHandler() {
        return eventHandler;
    }
}
