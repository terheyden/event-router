package com.terheyden.event;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vavr.CheckedConsumer;

/**
 * Manages event subscriptions.
 */
class EventSubscriberManager<T> {

    /**
     * Map of {@code [ event : [ sub1, sub2, ... ] ]}.
     * Events can be a {@code Class<?>} for long-running events
     * or a UUID for short-lived, publishAndReturn events.
     * (Or really any object that can be used as a key in a map.)
     */
    private final Queue<EventSubscription<T>> subscribers = new ConcurrentLinkedQueue<>();
    private final Collection<EventSubscription<T>> subscribersReadOnly = Collections.unmodifiableCollection(subscribers);

    UUID subscribe(CheckedConsumer<T> eventHandler) {
        EventSubscription<T> subscription = new EventSubscription<>(eventHandler);
        subscribers.add(subscription);
        return subscription.getSubscriptionId();
    }

    /**
     * Remove a subscription by its UUID.
     */
    void unsubscribe(UUID subscriptionId) {
        // This could be more performant.
        // Removes should be pretty uncommon,
        // and the number of events shouldn't be terribly high.
        // Concurrent, so we don't need to synchronize.
        subscribers
            .stream()
            .filter(sub -> sub.getSubscriptionId().equals(subscriptionId))
            .forEach(subscribers::remove);
    }

    public Collection<EventSubscription<T>> getSubscribers() {
        return subscribersReadOnly;
    }
}
