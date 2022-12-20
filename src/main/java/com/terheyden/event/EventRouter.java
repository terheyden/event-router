package com.terheyden.event;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import io.vavr.CheckedConsumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * The user can always make it static if they want.
 */
public class EventRouter {

    private static final Logger LOG = getLogger(EventRouter.class);

    /**
     * {@code [ Event.class : [ sub1, sub2, ... ] ]}.
     */
    private final EventSubscriberMap eventSubscriberMap = new EventSubscriberMap();

    /**
     * Thread-safe queue of events waiting to be delivered.
     */
    private final Queue<EventRequest> eventDeliveryQueue = new ConcurrentLinkedQueue<>();

    /**
     * Flag so new events can't interrupt current ones.
     * E.g. processing event A calls publish(event B), which should be queued.
     */
    private final AtomicBoolean isPublishing = new AtomicBoolean(false);

    /**
     * Defines the publish strategy.
     * Are messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final EventPublisher eventPublisher;

    /**
     * Use {@link EventRouterBuilder}.
     */
    /*package*/ EventRouter(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Begin building a new {@link EventRouter}.
     */
    public static EventRouterBuilder builder() {
        return new EventRouterBuilder();
    }

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    public <T> UUID subscribe(Class<T> eventClass, CheckedConsumer<T> eventHandler) {
        return eventSubscriberMap.add(eventClass, eventHandler);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler was subscribed to.
     * @param subscriptionId The UUID returned by the subscribe() method.
     * @return True if the subscription was found and removed.
     */
    public <T> boolean unsubscribe(Class<T> eventClass, UUID subscriptionId) {
        return eventSubscriberMap.remove(eventClass, subscriptionId);
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code publish("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event The event to publish.
     * @param eventClass The event class type. It may be useful to specify this if this event object type
     *                   is a subclass of the subscribed event class type.
     */
    public void publish(Object event, Class<?> eventClass) {
        try {
            // First just add the event to the delivery queue.
            eventDeliveryQueue.add(new EventRequest(event, eventClass));

            // Then run the event deliverer.
            deliverEvents();

        } catch (Exception e) {
            EventUtils.throwUnchecked(e);
        }
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code publish("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event The event to publish.
     */
    public void publish(Object event) {
        publish(event, event.getClass());
    }

    /**
     * Will begin delivering events to publishers if there are events in the queue,
     * and we are not already doing so.
     * <p>
     * If e.g. delivering event A has caused event B to be published, then event B will be
     * added to the queue and this method will be called again, but will short-circuit.
     */
    private void deliverEvents() {

        // If we're already delivering events, don't interrupt.
        boolean canLockPublisher = isPublishing.compareAndSet(false, true);
        if (!canLockPublisher) {
            LOG.debug("Event delivery already in-progress, skipping.");
            return;
        }

        // This try..finally is to make sure we unlock the publisher at the very end.
        try {
            // poll() will return null if the queue is empty.
            EventRequest eventReq = eventDeliveryQueue.poll();
            LOG.debug("Starting event delivery.");

            while (eventReq != null) {

                Object event = eventReq.event();
                Class<?> eventClass = eventReq.eventClass();
                List<EventSubscription> subscribers = eventSubscriberMap.find(eventClass);

                if (subscribers.size() > 0) {
                    try {

                        LOG.debug(
                            "Publishing event type '{}' to {} subscribers.",
                            eventClass.getSimpleName(),
                            subscribers.size());

                        eventPublisher.publish(this, event, subscribers);

                    } catch (Exception e) {
                        // Publishers are expected to deal with exceptions.
                        throw new IllegalStateException("EventPublisher threw an exception.", e);
                    }
                }

                // Ready for the next event.
                eventReq = eventDeliveryQueue.poll();
            }

        } finally {
            LOG.debug("Event delivery finished.");
            isPublishing.set(false);
        }
    }

    /**
     * Combines an event with its class type, for queuing and delivery.
     */
    private record EventRequest(Object event, Class<?> eventClass) { }
}
