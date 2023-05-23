package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class AbstractEventRouter<T> {

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
    private final EventSubscriberManager subscriberManager;

    /**
     * The singular thread pool used by all components.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Create a new event router with a custom thread pool.
     */
    protected AbstractEventRouter(
        ThreadPoolExecutor threadPoolExecutor,
        SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy) {

        this.receivedEventHandler = new ReceivedEventHandler<>(threadPoolExecutor);
        this.sendEventToSubscriberStrategy = sendEventToSubscriberStrategy;
        this.subscriberManager = new EventSubscriberManager();
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * Create a new event router with a dynamic thread pool of the given size.
     */
    protected AbstractEventRouter(
        int threadPoolSize,
        SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy) {

        this(ThreadPools.newDynamicThreadPool(threadPoolSize), sendEventToSubscriberStrategy);
    }

    /**
     * Create a new event router with a dynamic thread pool of the default size ({@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}).
     */
    protected AbstractEventRouter(SendEventToSubscriberStrategy<T> sendEventToSubscriberStrategy) {
        this(EventRouterGlobals.DEFAULT_THREADPOOL_SIZE, sendEventToSubscriberStrategy);
    }

    protected void publishInternal(EventRequest<T> eventRequest) {

        PublishRequest<T> request = new PublishRequest<>(
            eventRequest,
            sendEventToSubscriberStrategy,
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

    protected EventSubscriberManager getSubscriberManager() {
        return subscriberManager;
    }

    /**
     * For testing only.
     */
    Collection<EventSubscription> getSubscribers() {
        return subscriberManager.getSubscribers();
    }
}
