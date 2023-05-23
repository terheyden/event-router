package com.terheyden.event;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import io.vavr.CheckedConsumer;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventRouterImpl<T> extends AbstractEventRouter<T> implements EventRouter<T> {

    /**
     * Create a new event router with a custom thread pool.
     */
    public EventRouterImpl(ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor, new ThreadPoolSendStrategy<>(threadPoolExecutor));
    }

    /**
     * Create a new event router with a dynamic thread pool of the given size.
     */
    public EventRouterImpl(int threadPoolSize) {
        this(ThreadPools.newDynamicThreadPool(threadPoolSize));
    }

    /**
     * Create a new event router with a dynamic thread pool of the default size ({@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}).
     */
    public EventRouterImpl() {
        this(EventRouterGlobals.DEFAULT_THREADPOOL_SIZE);
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
        EventRouterSubscription<T> subscription = new EventRouterSubscription<>(eventHandler);
        getSubscriberManager().subscribe(subscription);
        return subscription.getSubscriptionId();
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param subscriptionId The UUID returned by the subscribe() method.
     */
    @Override
    public void unsubscribe(UUID subscriptionId) {
        getSubscriberManager().unsubscribe(subscriptionId);
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
        publishInternal(new EventRequest<>(eventObj));
    }
}
