package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedFunction1;

/**
 * A publish-subsribe event router with modifiable events.
 * Subscribers are called in the order they subscribed,
 * and may modify, replace, or cancel the event.
 *
 * @see EventRouter
 * @see EventQuery
 */
public interface ModifiableEventRouter<T> extends EventActor {

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventType Events are defined by their class type.
     *                  This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    UUID subscribe(CheckedFunction1<T, T> eventHandler);

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
    void publish(T eventObj);
}