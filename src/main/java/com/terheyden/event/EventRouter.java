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
     * @param eventType Events are defined by their class type.
     *                  This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    public <T> UUID subscribe(Class<T> eventType, CheckedConsumer<T> eventHandler) {
        // subscribe() expectes a consumer, but we store it as a function.
        CheckedFunction1<Class<?>, Object> eventFunc = EventUtils.consumerToFunction(eventHandler);
        return subscriber.subscribe(eventType, eventFunc);
    }

    public <T, R> UUID subscribeAndReply(Class<T> eventType, CheckedFunction1<T, R> eventHandler) {
        return subscriber.subscribe(eventType, eventHandler);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param eventType      Events are defined by their class type.
     *                       This is the type of event that the handler was subscribed to.
     * @param subscriptionId The UUID returned by the subscribe() method.
     * @return True if the subscription was found and removed.
     */
    public <T> boolean unsubscribe(Class<T> eventType, UUID subscriptionId) {
        return subscriber.unsubscribe(eventType, subscriptionId);
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code publish("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event     The event to publish.
     * @param eventType The event class type. It may be useful to specify this if this event object type
     *                  is a subclass of the subscribed event class type.
     */
    public void publish(Object event, Class<?> eventType) {
        publishInternal(eventPublisher, event, eventType);
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code publish("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event The event to publish.
     */
    public void publish(Object event) {
        publishInternal(eventPublisher, event);
    }

    public void publishAsync(Object event, Class<?> eventClass) {
        publishInternal(eventPublisherAsync, event, eventClass);
    }

    public void publishAsync(Object event) {
        publishInternal(eventPublisherAsync, event);
    }

    private void publishInternal(EventPublisher publisher, Object event, Class<?> eventClass) {

        PublishRequest request = new PublishRequest(
            this,
            event,
            eventClass,
            publisher,
            subscriber.findSubscribers(eventClass));

        this.publisher.publish(request);
    }

    private void publishInternal(EventPublisher publisher, Object event) {
        publishInternal(publisher, event, event.getClass());
    }

    public <T, R> CompletableFuture<R> query(T event, Class<R> expectedReplyType) {
        return queryInternal(eventPublisher, event, expectedReplyType);
    }

    public <T, R> CompletableFuture<R> queryAsync(T event, Class<R> expectedReplyType) {
        return queryInternal(eventPublisherAsync, event, expectedReplyType);
    }

    @SuppressWarnings("unchecked")
    <T, R> CompletableFuture<R> queryInternal(EventPublisher publisher, T event, Class<R> expectedReplyType) {

        // The expected reply type is just to help with generic typing.
        assert expectedReplyType != null;
        // The subscriber should complete this.
        CompletableFuture<Object> callbackFuture = new CompletableFuture<>();

        QueryRequest request = new QueryRequest(
            this,
            event,
            event.getClass(),
            publisher,
            subscriber.findSubscribers(event.getClass()),
            callbackFuture);

        this.publisher.query(request);
        return (CompletableFuture<R>) callbackFuture;
    }
}
