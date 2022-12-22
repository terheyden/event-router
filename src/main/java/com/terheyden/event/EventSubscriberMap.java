package com.terheyden.event;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A map of event class types to their subscriptions.
 * E.g. {@code Map<K, List<EventSubscription>>}.
 */
public class EventSubscriberMap {

    /**
     * Map of {@code [ event : [ sub1, sub2, ... ] ]}.
     * Events can be a {@code Class<?>} for long-running events
     * or a UUID for short-lived, publishAndReturn events.
     * (Or really any object that can be used as a key in a map.)
     */
    private final Map<EventKey, Queue<EventSubscription>> eventMap = new ConcurrentHashMap<>();

    public void add(SubscribeRequest subscribeRequest) {

        EventSubscription subscription = EventSubscription.createNew(
            subscribeRequest.subscriptionId(),
            subscribeRequest.eventHandler());

        // We don't want subscriptions changing while we're iterating over them.
        // So we want to use a Concurrent- collection.
        eventMap
            .computeIfAbsent(subscribeRequest.eventKey(), clazz -> new ConcurrentLinkedQueue<>())
            .add(subscription);
    }

    public boolean remove(UnsubscribeRequest unsubscribeRequest) {

        EventKey key = unsubscribeRequest.eventKey();
        Queue<EventSubscription> subscriptions = eventMap.get(key);
        if (subscriptions == null) {
            return false;
        }

        // We could've hashed by UUID, but what if there was a collision in one map
        // and not the other ... it's best to use a single collection / source of truth.
        UUID removeId = unsubscribeRequest.subscriptionId();
        return subscriptions.removeIf(subscription -> subscription.getSubscriptionId().equals(removeId));
    }

    /**
     * Find all subscribers for the given event class.
     *
     * @return List of subscribers, or empty list if none. The queue is a {@link ConcurrentLinkedQueue},
     *         so it won't throw a {@link ConcurrentModificationException}, but the size
     *         may be off if the queue is being modified at the same time.
     */
    public Queue<EventSubscription> find(EventKey uuidClass) {
        return eventMap.getOrDefault(uuidClass, EmptyQueue.instance());
    }
}
