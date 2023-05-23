package com.terheyden.event;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vavr.CheckedConsumer;

/**
 * A map of event class types to their subscriptions.
 * E.g. {@code Map<K, List<EventSubscription>>}.
 */
class EventSubscriberMap {

    /**
     * Map of {@code [ event : [ sub1, sub2, ... ] ]}.
     * Events can be a {@code Class<?>} for long-running events
     * or a UUID for short-lived, publishAndReturn events.
     * (Or really any object that can be used as a key in a map.)
     */
    private final Map<Class<?>, Queue<EventSubscription>> eventMap = new ConcurrentHashMap<>();

    public <T> UUID add(Class<T> eventType, CheckedConsumer<T> eventHandler) {

        EventSubscription subscription = new EventRouterSubscription(eventHandler);

        // We don't want subscriptions changing while we're iterating over them.
        // So we want to use a concurrent collection here.
        eventMap
            .computeIfAbsent(eventType, clazz -> new ConcurrentLinkedQueue<>())
            .add(subscription);

        return subscription.getSubscriptionId();
    }

    /**
     * Remove a subscription by its UUID.
     */
    boolean remove(UUID subscriptionId) {

        // This could be more performant.
        // Removes should be pretty uncommon,
        // and the number of events shouldn't be terribly high.
        // Concurrent, so we don't need to synchronize.
        return eventMap.values()
            .stream()
            .map(eventSubs -> eventSubs.removeIf(sub -> sub.getSubscriptionId().equals(subscriptionId)))
            .anyMatch(wasRemoved -> wasRemoved);
    }

    /**
     * Find all subscribers for the given event class.
     *
     * @return List of subscribers, or empty list if none. The queue is a {@link ConcurrentLinkedQueue},
     *         so it won't throw a {@link ConcurrentModificationException}, but the size
     *         may be off if the queue is being modified at the same time.
     */
    public Queue<EventSubscription> find(Class<?> uuidClassKey) {
        return eventMap.getOrDefault(uuidClassKey, EmptyQueue.instance());
    }
}
