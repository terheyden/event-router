package com.terheyden.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Each {@link EventRouter} event is handled by a separate thread.
 * An event's subscribers are called, in-order, on that single thread.
 */
public class ThreadPerEventPublisher extends BaseThreadPoolPublisher {

    public ThreadPerEventPublisher(int maxThreadCount) {
        super(maxThreadCount);
    }

    public ThreadPerEventPublisher(ExecutorService threadPool) {
        super(threadPool);
    }

    @Override
    public void publish(EventRouter sourceRouter, Object event, List<EventSubscription> subscribers) {
        // "Per event" means we'll call subscribers in-order.
        execute(() -> publishAllChecked(sourceRouter, subscribers, event));
    }

    public <T> CompletableFuture<T> publishAndReturn(EventRouter sourceRouter, Object event, List<EventSubscription> subscribers) {
        return CompletableFuture.supplyAsync(() -> {
            publishAllChecked(sourceRouter, subscribers, event);
            return null;
        }, threadPool);
    }
}
