package com.terheyden.event;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
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
     * {@code [ UUID : [ sub ] ]}.
     */
    private final EventSubscriberMap eventSubscriberMap = new EventSubscriberMap();

    /**
     * Thread-safe queue of events waiting to be delivered.
     */
    private final Queue<EventRequest> eventDeliveryQueue = new ArrayDeque<>();

    private final Queue<SubscribeRequest> subscribeQueue = new ArrayDeque<>();
    private final Queue<UnsubscribeRequest> unsubscribeQueue = new ArrayDeque<>();

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
     * Create a new event router with the settings provided in the config object.
     */
    public EventRouter(EventRouterConfig config) {
        this.eventPublisher = config.eventPublisher();
    }

    /**
     * Uses default config with direct publishing (uses calling thread) to create a new event router.
     */
    public EventRouter() {
        this(new EventRouterConfig());
    }

    /**
     * When an event of type {@code uuidClass} is published, {@code eventHandler} will be called.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    public <T> UUID subscribe(Class<T> eventClass, CheckedConsumer<T> eventHandler) {
        // First just add the request to the queue.
        EventKey eventKey = new EventKey(eventClass);
        SubscribeRequest request = new SubscribeRequest(eventKey, eventHandler);
        subscribeQueue.add(request);
        // Then poke the job handler.
        deliverEvents();
        return request.subscriptionId();
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler was subscribed to.
     * @param subscriptionId The UUID returned by the subscribe() method.
     */
    public <T> void unsubscribe(Class<T> eventClass, UUID subscriptionId) {
        // First just add the request to the queue.
        EventKey eventKey = new EventKey(eventClass);
        UnsubscribeRequest request = new UnsubscribeRequest(eventKey, subscriptionId);
        unsubscribeQueue.add(request);
        // Then poke the job handler.
        deliverEvents();
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
            SubscribeRequest subscribeRequest = subscribeQueue.poll();
            while (subscribeRequest != null) {
                eventSubscriberMap.add(subscribeRequest);
                subscribeRequest = subscribeQueue.poll();
            }

            UnsubscribeRequest unsubscribeRequest = unsubscribeQueue.poll();
            while (unsubscribeRequest != null) {
                eventSubscriberMap.remove(unsubscribeRequest);
                unsubscribeRequest = unsubscribeQueue.poll();
            }

            EventRequest eventReq = eventDeliveryQueue.poll();
            LOG.debug("Starting event delivery.");

            while (eventReq != null) {
                publishEventRequest(eventReq);
                eventReq = eventDeliveryQueue.poll();
            }

        } finally {
            LOG.debug("Event delivery finished.");
            isPublishing.set(false);
        }
    }

    /**
     * Publish a single vetted event request.
     */
    private void publishEventRequest(EventRequest eventReq) {

        Object event = eventReq.event();
        Object uuidClass = eventReq.uuidClass();
        List<EventSubscription> subscribers = eventSubscriberMap.find(uuidClass);

        if (subscribers.size() > 0) {
            try {

                LOG.debug(
                    "Publishing event type '{}' to {} subscribers.",
                    uuidClass.toString(),
                    subscribers.size());

                // TODO: What if a sub unsubscribes in the middle of this?
                eventPublisher.publish(this, event, subscribers);

            } catch (Exception e) {
                // Publishers are expected to deal with exceptions.
                throw new IllegalStateException("EventPublisher threw an exception.", e);
            }
        }
    }
}
