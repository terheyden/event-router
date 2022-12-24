package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * The user can always make it static if they want.
 */
public class EventRouterPublisher {

    private static final Logger LOG = getLogger(EventRouter.class);

    /**
     * {@code [ Event.class : [ sub1, sub2, ... ] ]}.
     * {@code [ UUID : [ sub ] ]}.
     */
    private final EventSubscriberMap eventSubscriberMap = new EventSubscriberMap();

    /**
     * Defines the direct publish strategy.
     * Are messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final EventPublisher eventPublisher;

    /**
     * Defines the async publish strategy.
     * Are messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final EventPublisher eventPublisherAsync;

    /**
     * Events should be processed in-order. If publishing one event causes another,
     * at least wait until the current event is finished.
     */
    private final Queue<PublishRequest> publishRequests = new ConcurrentLinkedQueue<>();

    /**
     * Publish requests should finish in-order.
     * To enforce this, we use a lock.
     */
    private final Semaphore publishLock = new Semaphore(1);

    /**
     * Create a new event router with the settings provided in the config object.
     */
    public EventRouterPublisher(EventRouterConfig config) {
        this.eventPublisher = config.eventPublisher();
        this.eventPublisherAsync = config.eventPublisherAsync();
    }

    /**
     * Uses default config with direct publishing (uses calling thread) to create a new event router.
     */
    public EventRouterPublisher() {
        this(new EventRouterConfig());
    }

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    public <T> UUID subscribe(Class<T> eventClass, CheckedConsumer<T> eventHandler) {
        // subscribe() expectes a consumer, but we store it as a function.
        ConsumerFunction1<Object, Object> eventFunc = consumerToFunction(eventHandler);
        return subscribeInternal(eventClass, eventFunc);
    }

    public <T, R> UUID subscribeAndReply(Class<T> eventClass, CheckedFunction1<T, R> eventHandler) {
        return subscribeInternal(eventClass, eventHandler);
    }

    public <T, R> UUID subscribeInternal(Object uuidClass, CheckedFunction1<T, R> eventHandler) {
        // Concurrent, so we don't need to synchronize.
        CheckedFunction1<Object, Object> eventFunc = functionToFunction(eventHandler);
        return eventSubscriberMap.add(uuidClass, eventFunc);
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
        // Concurrent, so we don't need to synchronize.
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
        publishInternal(event, eventClass, eventPublisher);
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

    public void publishAsync(Object event, Class<?> eventClass) {
        publishInternal(event, eventClass, eventPublisherAsync);
    }

    public void publishAsync(Object event) {
        publishAsync(event, event.getClass());
    }

    /**
     * Internal publish that works with UUIDs.
     */
    /*package*/ void publishInternal(Object event, Object uuidClass, EventPublisher publisher) {
        // First, put the event on the queue.
        publishRequests.add(new PublishRequest(event, uuidClass, publisher));
        // Then, process the queue.
        processAllPublishRequests();
    }

    @SuppressWarnings("unchecked")
    public <T, R> CompletableFuture<R> query(T event, Class<R> expectedReplyType) {

        // Tell the publish request that this is a query.
        CompletableFuture<Object> callbackFuture = new CompletableFuture<>();
        PublishRequest publishRequest = new PublishRequest(event, event.getClass(), eventPublisher, callbackFuture);
        publishRequests.add(publishRequest);
        // Then, process the queue.
        processAllPublishRequests();
        return (CompletableFuture<R>) callbackFuture;
    }

    /**
     * Take from the publish request queue and deliver to subscribers
     * until the queue is empty again.
     */
    private void processAllPublishRequests() {

        // If someone's already processing the queue, don't do it again.
        // This prevents the same thread from being interrupted by a second publish.
        if (!publishLock.tryAcquire()) {
            return;
        }

        // Put everything in a try so we can guarantee the lock will be released.
        try {

            @Nullable PublishRequest publishRequest = publishRequests.poll();

            while (publishRequest != null) {

                processPublishRequest(publishRequest);
                // Get the next event.
                publishRequest = publishRequests.poll();
            }

        } finally {
            publishLock.release();
        }
    }

    /**
     * Deliver the given publish request to subscribers.
     */
    private void processPublishRequest(PublishRequest publishRequest) {

        Object event = publishRequest.event();
        Object eventKey = publishRequest.eventKey();
        Queue<EventSubscription> subscribers = eventSubscriberMap.find(eventKey);

        if (subscribers.isEmpty()) {
            handleNoSubscribersEvent(event, eventKey);
            return;
        }

        LOG.debug(
            "Publishing event type '{}' to {} subscribers.",
            eventKey.toString(),
            subscribers.size());

        EventPublisher publisher = publishRequest.eventPublisher();

        if (publishRequest.callbackFuture().isPresent()) {
            // This is a query.
            CompletableFuture<Object> callbackFuture = publishRequest.callbackFuture().get();
            publisher.query(event, subscribers, callbackFuture);
        } else {
            // This is a regular publish.
            publisher.publish(event, subscribers);
        }
    }

    /**
     * If it's a user event with no subscribers, send a {@link NoSubscribersEvent}.
     */
    private void handleNoSubscribersEvent(Object event, Object eventKey) {

        // If it's not a class, it's an internal event.
        if (!(eventKey instanceof Class)) {
            return;
        }

        Class<?> eventClassKey = (Class<?>) eventKey;
        // https://stackoverflow.com/questions/12145185/determine-if-a-class-implements-a-interface-in-java
        boolean internalEvent = SpecialEvent.class.isAssignableFrom(eventClassKey);

        if (internalEvent) {
            // Avoid infinite loop.
            return;
        }

        LOG.debug("No subscribers for event: {}", event);
        publish(new NoSubscribersEvent(event, eventClassKey));
    }

    @SuppressWarnings("unchecked")
    private static <T> ConsumerFunction1<Object, Object> consumerToFunction(CheckedConsumer<T> eventHandler) {
        return new ConsumerFunction1<Object, Object>((CheckedConsumer) eventHandler);
    }

    @SuppressWarnings("unchecked")
    private static <T, R> CheckedFunction1<Object, Object> functionToFunction(CheckedFunction1<T, R> eventHandler) {
        return (CheckedFunction1<Object, Object>) eventHandler;
    }
}
