package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Used to configure a new {@link EventRouter}.
 */
public class EventRouterConfig {

    private ThreadPoolExecutor receivedEventHandlerThreadPool = ThreadPools.newDynamicThreadPool();
    private SendEventToSubscriberStrategy sendEventToSubscriberStrategy = new SequentialSendStrategy();

    public ThreadPoolExecutor receivedEventHandlerThreadPool() {
        return receivedEventHandlerThreadPool;
    }

    /**
     * This is the thread pool that processes sendEventToSubscribers requests
     * (it doesn't send events to subscribers).
     * <p>
     * By default, a dynamically-scaling thread pool is used
     * that scales between 0 and [CPU processors - 1] threads.
     *
     * @see ThreadPools#newDynamicThreadPool() for more info.
     */
    public EventRouterConfig receivedEventHandlerThreadPool(ThreadPoolExecutor receivedEventHandlerThreadPool) {
        this.receivedEventHandlerThreadPool = receivedEventHandlerThreadPool;
        return this;
    }

    public SendEventToSubscriberStrategy sendEventToSubscriberStrategy() {
        return sendEventToSubscriberStrategy;
    }

    /**
     * This defines the strategy used to send events to subscribers.
     */
    public EventRouterConfig sendEventToSubscriberStrategy(SendEventToSubscriberStrategy sendEventToSubscriberStrategy) {
        this.sendEventToSubscriberStrategy = sendEventToSubscriberStrategy;
        return this;
    }
}
