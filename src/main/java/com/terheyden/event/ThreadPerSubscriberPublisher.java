package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * When an {@link EventRouter} event is received, all subscribers are notified asynchronously.
 */
public class ThreadPerSubscriberPublisher extends BaseThreadPoolPublisher {

    public ThreadPerSubscriberPublisher(int maxThreadCount) {
        super(maxThreadCount);
    }

    public ThreadPerSubscriberPublisher(ExecutorService threadPool) {
        super(threadPool);
    }

    @Override
    public void publish(EventRouter sourceRouter, Object event, Collection<EventSubscription> subscribers) {
        // No ordering required, every subscriber gets notified at once.
        subscribers.forEach(sub -> execute(() -> sub.getEventHandler().unchecked().apply(event)));
    }

    @Override
    public void query(
        EventRouter sourceRouter,
        Object event,
        Collection<EventSubscription> subscribers,
        UUID callbackEventKey) {

        subscribers.forEach(sub -> execute(() -> {
            Object result = sub.getEventHandler().unchecked().apply(event);
            sourceRouter.publishInternal(result, callbackEventKey);
        }));
    }
}
