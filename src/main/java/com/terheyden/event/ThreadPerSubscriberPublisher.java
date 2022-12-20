package com.terheyden.event;

import java.util.List;
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
    public void publish(EventRouter sourceRouter, Object event, List<EventSubscription> subscribers) {
        // No ordering required, every subscriber gets notified at once.
        for (EventSubscription subscriber : subscribers) {
            execute(() -> publishChecked(sourceRouter, subscriber, event));
        }
    }
}
