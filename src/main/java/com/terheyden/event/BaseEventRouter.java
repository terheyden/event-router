package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class BaseEventRouter<T> {

    /**
     * All "publish this event" requests are delegated to this class.
     */
    private final ReceivedEventHandler<T> receivedEventHandler;

    /**
     * Send events to subscribers.
     * Decides if messages sent directly (on the calling thread), multi-thread, in-order, etc.
     */
    private final SendEventStrategy<T> sendEventStrategy;

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
    protected BaseEventRouter(
        ThreadPoolExecutor threadPoolExecutor,
        SendEventStrategy<T> sendEventStrategy) {

        this.receivedEventHandler = new ReceivedEventHandler<>(threadPoolExecutor);
        this.sendEventStrategy = sendEventStrategy;
        this.subscriberManager = new EventSubscriberManager();
        this.threadPoolExecutor = threadPoolExecutor;
    }

    protected void publishInternal(EventRequest<T> eventRequest) {

        PublishRequest<T> request = new PublishRequest<>(
            eventRequest,
            sendEventStrategy,
            subscriberManager.getSubscribers());

        receivedEventHandler.publish(request);
    }

    /**
     * The singular thread pool used by all components in this event router.
     * For metrics only — don't use this to publish events.
     */
    protected ThreadPoolExecutor getThreadPool() {
        return threadPoolExecutor;
    }

    /**
     * Used by subclasses to manage subscriptions.
     */
    protected EventSubscriberManager getSubscriberManager() {
        return subscriberManager;
    }

    /**
     * For testing / reporting.
     */
    protected Collection<EventSubscription> getSubscribers() {
        return subscriberManager.getSubscribers();
    }
}
