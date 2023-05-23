package com.terheyden.event;

import java.util.Collection;

/**
 * Strategies for delivering events.
 * E.g. on the calling thread, on one separate thread, in a threadpool, etc.
 * <p>
 * These return thrown exceptions, so the caller can decide what to do with them.
 * This makes sense because the caller may want to handle exceptions differently
 * depending on the event type and/or the event publisher (e.g. direct reacts
 * very differently than a threadpool).
 */
public interface SendEventToSubscriberStrategy<T> {

    /**
     * A sendEventToSubscribers request has been made. This publisher should use its preferred strategy
     * to deliver the event to the subscribers. The list of subscribers is FIFO according to
     * the order in which they were subscribed (not that the publisher needs to obey that
     * depending on the strategy, but it could).
     *
     * @param event       The event to deliver to each {@link EventSubscription}.
     * @param subscribers The concurrent collection of subscribers to deliver the event to, guaranteed to be non-empty.
     */
    void sendEventToSubscribers(T event, Collection<EventSubscription<T>> subscribers);

    String getMetrics();
}
