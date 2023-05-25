package com.terheyden.event;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages event subscriptions.
 */
class EventSubscriberManager {

    /**
     * Map of {@code [ event : [ sub1, sub2, ... ] ]}.
     * Events can be a {@code Class<?>} for long-running events
     * or a UUID for short-lived, publishAndReturn events.
     * (Or really any object that can be used as a key in a map.)
     */
    private final Queue<EventSubscription> subscribers = new ConcurrentLinkedQueue<>();
    private final Collection<EventSubscription> subscribersReadOnly = Collections.unmodifiableCollection(subscribers);

    void subscribe(EventSubscription subscription) {
        subscribers.add(subscription);
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

    public Collection<EventSubscription> getSubscribers() {
        return subscribersReadOnly;
    }

    Queue<EventSubscription> getSubscribersInternal() {
        return subscribers;
    }
}
