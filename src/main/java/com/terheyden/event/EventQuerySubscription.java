package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedFunction1;

/**
 * An {@link EventQueryImpl} subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class EventQuerySubscription<I, O> implements EventSubscription {

    private final UUID subscriptionId;
    private final CheckedFunction1<I, O> eventHandler;

    EventQuerySubscription(
        UUID subscriptionId,
        CheckedFunction1<I, O> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    EventQuerySubscription(CheckedFunction1<I, O> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedFunction1<I, O> getEventHandler() {
        return eventHandler;
    }
}
