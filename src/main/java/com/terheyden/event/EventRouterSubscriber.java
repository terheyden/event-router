package com.terheyden.event;

import java.util.Queue;
import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class EventRouterSubscriber {

    /**
     * {@code [ Event.class : [ sub1, sub2, ... ] ]}.
     * {@code [ UUID : [ sub ] ]}.
     */
    private final EventSubscriberMap eventSubscriberMap = new EventSubscriberMap();

    <T> UUID subscribe(Class<T> eventType, CheckedConsumer<T> eventHandler) {
        return eventSubscriberMap.add(eventType, eventHandler);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param eventType Events are defined by their class type.
     *                   This is the type of event that the handler was subscribed to.
     * @param subscriptionId The UUID returned by the subscribe() method.
     * @return True if the subscription was found and removed.
     */
    boolean unsubscribe(UUID subscriptionId) {
        // Concurrent, so we don't need to synchronize.
        return eventSubscriberMap.remove(subscriptionId);
    }

    Queue<EventSubscription> findSubscribers(Class<?> eventType) {
        return eventSubscriberMap.find(eventType);
    }
}
