package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Publishes events to subscribers on a thread pool.
 */
public class PerSubscriberPublisher implements EventPublisher {

    private final ThreadPoolExecutor threadpool;

    public PerSubscriberPublisher(ThreadPoolExecutor threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    public void publish(Object event, Queue<EventSubscription> subscribers) {
        subscribers.forEach(sub ->
            threadpool.execute(() ->
                sub.getEventHandler().unchecked().accept(event)));
    }
}
