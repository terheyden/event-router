package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * Used by {@link EventSubscriberMap} to store a subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class EventRouterSubscription<T> implements EventSubscription {

    private final UUID subscriptionId;
    private final CheckedConsumer<T> eventHandler;

    EventRouterSubscription(
        UUID subscriptionId,
        CheckedConsumer<T> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    EventRouterSubscription(CheckedConsumer<T> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedConsumer<T> getEventHandler() {
        return eventHandler;
    }
}
