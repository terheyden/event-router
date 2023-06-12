package com.terheyden.event;

import java.util.UUID;

/**
 * A publish-subscribe event router that sends query events that return a response.
 *
 * @see EventRouter
 * @see ModifiableEventRouter
 */
public interface EventQuery<I, O> extends EventSubscriber {

    /**
     * Subscribe to receive events sent by this router.
     *
     * @param eventHandler The event handler to call when an event is received.
     * @return A UUID that can later be used to unsubscribe.
     */
    UUID subscribe(CheckedFunction<I, O> eventHandler);

    /**
     * A specialized form of {@link EventRouter#publish(Object)}. Publish the given event object
     * to all subscribers, and expect a response.
     *
     * @param eventObj The event object to publish.
     * @param responseHandler callback function to call when a response object is received.
     */
    void query(I eventObj, CheckedConsumer<O> responseHandler);
}
