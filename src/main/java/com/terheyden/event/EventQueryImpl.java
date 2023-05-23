package com.terheyden.event;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventQueryImpl<I, O> extends AbstractEventRouter<I> implements EventQuery<I, O> {

    /**
     * Create a new event router with a custom thread pool.
     */
    public EventQueryImpl(ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor, new EventQuerySendStrategy<>(threadPoolExecutor));
    }

    /**
     * Create a new event router with a dynamic thread pool of the given size.
     */
    public EventQueryImpl(int threadPoolSize) {
        this(ThreadPools.newDynamicThreadPool(threadPoolSize));
    }

    /**
     * Create a new event router with a dynamic thread pool of the default size ({@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}).
     */
    public EventQueryImpl() {
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
    public UUID subscribe(CheckedFunction1<I, O> eventHandler) {
        EventQuerySubscription<I, O> subscription = new EventQuerySubscription<>(eventHandler);
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
    public void query(I eventObj, CheckedConsumer<O> responseHandler) {
        publishInternal(new QueryEventRequest<>(eventObj, responseHandler));
    }
}
