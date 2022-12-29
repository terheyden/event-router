package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Used to configure a new {@link EventRouter}.
 */
public class EventRouterConfig {

    private ThreadPoolExecutor publishExecutor = ThreadPools.newDynamicThreadPool();
    private EventPublisher eventPublisher = new PerEventPublisher();

    public ThreadPoolExecutor publishExecutor() {
        return publishExecutor;
    }

    /**
     * Set the thread pool used to publish events.
     * By default, a dynamically-scaling thread pool is used
     * that scales between 0 and [CPU processors - 1] threads.
     *
     * @see ThreadPools#newDynamicThreadPool() for more info.
     */
    public EventRouterConfig publishExecutor(ThreadPoolExecutor publishExecutor) {
        this.publishExecutor = publishExecutor;
        return this;
    }

    public EventPublisher eventPublisher() {
        return eventPublisher;
    }

    public EventRouterConfig eventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }
}
