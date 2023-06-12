package com.terheyden.event;

import java.util.UUID;

/**
 * A classic publish-subscribe event router.
 * Sends events to all subscribers using a thread pool.
 *
 * @see EventQuery
 * @see ModifiableEventRouter
 */
public interface EventRouter<T> extends EventSubscriber {

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @return A UUID that can later be used to unsubscribe.
     */
    UUID subscribe(CheckedConsumer<T> eventHandler);

    /**
     * Publish the given event to all subscribers of the event object's type.
     * This is a non-blocking call; events are always published asynchronously.
     * <p>
     * Example:
     * <pre>
     * {@code
     * // Subscribe to all String events:
     * eventRouter.subscribe(System.out::println);
     * // Publish a String event:
     * eventRouter.publish("Hello World!");
     * }
     * </pre>
     *
     * @param eventObj The event to send to all subscribers
     */
    void publish(T eventObj);
}
