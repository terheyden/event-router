package com.terheyden.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.vavr.CheckedConsumer;

import static java.util.Collections.emptyList;

/**
 * A map of event class types to their subscriptions.
 * E.g. {@code Map<Class<?>, List<EventSubscription>>}.
 */
public class EventSubscriberMap {

    private final Map<Class<?>, List<EventSubscription>> eventMap = new ConcurrentHashMap<>();

    public UUID add(Class<?> eventClass, CheckedConsumer<?> eventHandler) {

        EventSubscription subscription = EventSubscription.createNew(eventHandler);

        eventMap
            .computeIfAbsent(eventClass, clazz -> new ArrayList<>())
            .add(subscription);

        return subscription.getSubscriptionId();
    }

    public boolean remove(Class<?> eventClass, UUID subscriptionId) {

        List<EventSubscription> subscriptions = eventMap.get(eventClass);
        if (subscriptions == null) {
            return false;
        }

        // We could've hashed by UUID, but what if there was a collision in one map
        // and not the other ... it's best to use a single collection / source of truth.
        return subscriptions.removeIf(subscription -> subscription.getSubscriptionId().equals(subscriptionId));
    }

    /**
     * Find all subscribers for the given event class.
     *
     * @return List of subscribers, or empty list if none
     */
    public List<EventSubscription> find(Class<?> eventClass) {
        return eventMap.getOrDefault(eventClass, emptyList());
    }
}
