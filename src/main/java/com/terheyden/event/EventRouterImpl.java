package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import io.vavr.CheckedConsumer;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventRouterImpl<T> implements EventRouter<T> {

    /**
     * All "publish this event" requests are delegated to this class.
     */
    private final ReceivedEventHandler<T> receivedEventHandler;

    /**
     * Send events to subscribers.
     * Decides if messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy;

    /**
     * Manages event subscriptions.
     */
    private final EventSubscriberManager<T> subscriberManager;

    /**
     * The singular thread pool used by all components.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Create a new event router with a custom thread pool.
     */
    public EventRouterImpl(ThreadPoolExecutor threadPoolExecutor) {
        this.receivedEventHandler = new ReceivedEventHandler<>(threadPoolExecutor);
        this.sendEventToSubscriberStrategy = new ThreadPoolSendStrategy<>(threadPoolExecutor);
        this.subscriberManager = new EventSubscriberManager<>();
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * Create a new event router with a dynamic thread pool of the given size.
     */
    public EventRouterImpl(int threadPoolSize) {
        this(ThreadPools.newDynamicThreadPool(threadPoolSize));
    }

    /**
     * Create a new event router with a dynamic thread pool of the default size ({@link #DEFAULT_THREADPOOL_SIZE}).
     */
    public EventRouterImpl() {
        this(DEFAULT_THREADPOOL_SIZE);
    }

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventType Events are defined by their class type.
     *                  This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    @Override
    public UUID subscribe(CheckedConsumer<T> eventHandler) {
        return subscriberManager.subscribe(eventHandler);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param subscriptionId The UUID returned by the subscribe() method.
     */
    @Override
    public void unsubscribe(UUID subscriptionId) {
        subscriberManager.unsubscribe(subscriptionId);
    }

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
    @Override
    public void publish(T eventObj) {
        publishInternal(sendEventToSubscriberStrategy, eventObj);
    }

    private void publishInternal(SendEventToSubscriberStrategy<T> publisher, T event) {

        PublishRequest<T> request = new PublishRequest<>(
            this,
            event,
            publisher,
            subscriberManager.getSubscribers());

        receivedEventHandler.publish(request);
    }

    /**
     * The singular thread pool used by all components in this event router.
     * For metrics only — don't use this to publish events.
     */
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    /**
     * For testing only.
     */
    Collection<EventSubscription<T>> getSubscribers() {
        return subscriberManager.getSubscribers();
    }
}