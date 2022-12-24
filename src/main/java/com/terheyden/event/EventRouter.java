package com.terheyden.event;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventRouter {

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
     * All publish and query requests are delegated to this class.
     */
    private final EventRouterPublisher publisher;

    private final EventRouterSubscriber subscriber;

    /**
     * Create a new event router with the settings provided in the config object.
     */
    public EventRouter(EventRouterConfig config) {
        this.eventPublisher = config.eventPublisher();
        this.eventPublisherAsync = config.eventPublisherAsync();
        this.publisher = new EventRouterPublisher();
        this.subscriber = new EventRouterSubscriber();
    }

    /**
     * Uses default config with direct publishing (uses calling thread) to create a new event router.
     */
    public EventRouter() {
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
        return subscriber.subscribe(eventClass, eventHandler);
    }

    public <T, R> UUID subscribeAndReply(Class<T> eventClass, CheckedFunction1<T, R> eventHandler) {
        return subscriber.subscribeInternal(eventClass, eventHandler);
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
        return subscriber.unsubscribe(eventClass, subscriptionId);
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

        PublishRequest request = new PublishRequest(
            this,
            event,
            eventClass,
            eventPublisher,
            subscriber.findSubscribers(eventClass));

        publisher.publishInternal(request);
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

        PublishRequest request = new PublishRequest(
            this,
            event,
            eventClass,
            eventPublisherAsync,
            subscriber.findSubscribers(eventClass));

        publisher.publishInternal(request);
    }

    public void publishAsync(Object event) {
        publishAsync(event, event.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T, R> CompletableFuture<R> query(T event, Class<R> expectedReplyType) {

        // Tell the publish request that this is a query.
        CompletableFuture<Object> callbackFuture = new CompletableFuture<>();

        PublishRequest request = new PublishRequest(
            this, event,
            event.getClass(),
            eventPublisher,
            subscriber.findSubscribers(event.getClass()),
            callbackFuture);

        publisher.query(request);
        return (CompletableFuture<R>) callbackFuture;
    }
}
