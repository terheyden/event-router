package com.terheyden.event;

import java.util.Collection;
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
    public void publish(Object event, Collection<EventSubscription> subscribers) {
        // "Per event" means we'll call subscribers in-order.
        execute(() -> subscribers.forEach(sub -> sub.getEventHandler().unchecked().accept(event)));
    }
}
