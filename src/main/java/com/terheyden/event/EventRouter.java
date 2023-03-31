package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventRouter {

    /**
     * All "publish my event" requests are delegated to this class.
     */
    private final ReceivedEventHandler receivedEventHandler;

    /**
     * This is the thing that sends events to subscribers.
     * Are messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final SendEventToSubscriberStrategy sendEventToSubscriberStrategy;

    /**
     * Subscription delegate.
     */
    private final EventSubscriberManager subscriberManager;

    /**
     * Create a new event router with the settings provided in the config object.
     */
    public EventRouter(EventRouterConfig config) {
        this.receivedEventHandler = new ReceivedEventHandler(config.receivedEventHandlerThreadPool());
        this.sendEventToSubscriberStrategy = config.sendEventToSubscriberStrategy();
        this.subscriberManager = new EventSubscriberManager();
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
        return subscriberManager.subscribe(eventType, eventHandler);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param subscriptionId The UUID returned by the subscribe() method.
     * @return True if the subscription was found and removed.
     */
    public boolean unsubscribe(UUID subscriptionId) {
        return subscriberManager.unsubscribe(subscriptionId);
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code sendEventToSubscribers("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event     The event to sendEventToSubscribers.
     * @param eventType The event class type. It may be useful to specify this if this event object type
     *                  is a subclass of the subscribed event class type.
     */
    public void publish(Object event, Class<?> eventType) {
        publishInternal(sendEventToSubscriberStrategy, event, eventType);
    }

    /**
     * Publish an event to all subscribers of the event object's type.
     * For example, {@code sendEventToSubscribers("hello")} will call all subscribers of type {@code String.class}.
     *
     * @param event The event to sendEventToSubscribers.
     */
    public void publish(Object event) {
        publishInternal(sendEventToSubscriberStrategy, event);
    }

    private void publishInternal(SendEventToSubscriberStrategy publisher, Object event, Class<?> eventClass) {

        PublishRequest request = new PublishRequest(
            this,
            event,
            eventClass,
            publisher,
            subscriberManager.findSubscribers(eventClass));

        this.receivedEventHandler.publish(request);
    }

    private void publishInternal(SendEventToSubscriberStrategy publisher, Object event) {
        publishInternal(publisher, event, event.getClass());
    }

    public String getMetrics() {
        return String.format("Events Received (ReceivedEventHandler):\n%s\n\nSubscriber Deliveries (SendEventToSubscriberStrategy):\n%s",
            EventUtils.threadReport(receivedEventHandler.getEventRequestExecutor()),
            sendEventToSubscriberStrategy.getMetrics());
    }
}
