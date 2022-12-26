package com.terheyden.event;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vavr.CheckedFunction1;

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

    public UUID add(Class<?> eventType, CheckedFunction1<Object, Object> eventHandler) {

        EventSubscription subscription = EventSubscription.createNew(eventHandler);

        // We don't want subscriptions changing while we're iterating over them.
        // So we want to use a concurrent collection here.
        eventMap
            .computeIfAbsent(eventType, clazz -> new ConcurrentLinkedQueue<>())
            .add(subscription);

        return subscription.getSubscriptionId();
    }

    boolean remove(Class<?> eventType, UUID subscriptionId) {

        return find(eventType)
            .removeIf(subscription -> subscription.getSubscriptionId().equals(subscriptionId));
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
