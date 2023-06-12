package com.terheyden.event;

import java.util.UUID;

/**
 * A {@link ModifiableEventRouterImpl} subscription.
 * Has a UUID to identify it, and the handler to apply to incoming event objects.
 */
final class ModifiableEventSubscription<T> implements EventSubscription {

    private final UUID subscriptionId;
    private final CheckedFunction<T, T> eventHandler;

    ModifiableEventSubscription(
        UUID subscriptionId,
        CheckedFunction<T, T> eventHandler) {

        this.subscriptionId = subscriptionId;
        this.eventHandler = eventHandler;
    }

    ModifiableEventSubscription(CheckedFunction<T, T> eventHandler) {
        this(UUID.randomUUID(), eventHandler);
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CheckedFunction<T, T> getEventHandler() {
        return eventHandler;
    }
}
