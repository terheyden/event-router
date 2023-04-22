package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * Used by {@link EventSubscriberMap} to store a subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class EventSubscription {

    private final UUID subscriptionId;
    private final CheckedConsumer<Object> eventHandler;

    @SuppressWarnings("unchecked")
    EventSubscription(
        UUID subscriptionId,
        CheckedConsumer<?> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = (CheckedConsumer<Object>) eventHandler;
    }

    EventSubscription(CheckedConsumer<?> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedConsumer<Object> getEventHandler() {
        return eventHandler;
    }
}
