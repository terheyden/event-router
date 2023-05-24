package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedFunction1;

/**
 * A {@link ModifiableEventRouterImpl} subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class ModifiableEventSubscription<T> implements EventSubscription {

    private final UUID subscriptionId;
    private final CheckedFunction1<T, T> eventHandler;

    ModifiableEventSubscription(
        UUID subscriptionId,
        CheckedFunction1<T, T> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    ModifiableEventSubscription(CheckedFunction1<T, T> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedFunction1<T, T> getEventHandler() {
        return eventHandler;
    }
}
