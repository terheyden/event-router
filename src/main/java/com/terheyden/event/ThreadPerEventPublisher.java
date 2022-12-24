package com.terheyden.event;

import java.util.Collection;
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
    public void publish(EventRouter sourceRouter, Object event, Collection<EventSubscription> subscribers) {
        // "Per event" means we'll call subscribers in-order.
        execute(() -> subscribers.forEach(sub -> sub.getEventHandler().unchecked().apply(event)));
    }

    @Override
    public void query(
        EventRouter sourceRouter,
        Object event,
        Collection<EventSubscription> subscribers,
        CompletableFuture<Object> callbackFuture) {

        execute(() -> subscribers.forEach(sub -> {
            Object result = sub.getEventHandler().unchecked().apply(event);
            callbackFuture.complete(result);
        }));
    }
}
