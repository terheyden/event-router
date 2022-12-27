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
