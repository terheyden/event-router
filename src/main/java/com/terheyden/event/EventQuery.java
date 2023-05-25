package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * A publish-subscribe event router that sends query events that return a response.
 *
 * @see EventRouter
 * @see ModifiableEventRouter
 */
public interface EventQuery<I, O> extends EventSubscriber {

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventType Events are defined by their class type.
     *                  This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    UUID subscribe(CheckedFunction1<I, O> eventHandler);

    /**
     * Publish the given event to all subscribers of the event object's type.
     * This is a non-blocking call; events are always published asynchronously.
     * <p>
     * Example:
     * <pre>
     * {@code
     * // Subscribe to all String events:
     * eventRouter.subscribe(String.class, System.out::println);
     * // Publish a String event:
     * eventRouter.publish("Hello World!");
     * }
     * </pre>
     * Remember that if you're publishing a subclass of an event type,
     * you'll need to cast it to the correct type:
     * <pre>
     * {@code
     * eventRouter.subscribe(MainClass.class, this::handleMainClass);
     * eventRouter.publish((MainClass) subClassObj);
     * }
     * </pre>
     *
     * @param event The event to send to all subscribers
     */
    void query(I eventObj, CheckedConsumer<O> responseHandler);
}
