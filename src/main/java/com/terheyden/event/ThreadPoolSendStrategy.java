package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
public class ThreadPoolSendStrategy implements SendEventToSubscriberStrategy {

    private final ThreadPoolExecutor threadpool;

    public ThreadPoolSendStrategy(ThreadPoolExecutor threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    public void sendEventToSubscribers(Object event, Queue<EventSubscription> subscribers) {
        subscribers.forEach(sub ->
            threadpool.execute(() ->
                sub.getEventHandler().unchecked().accept(event)));
    }

    @Override
    public String getMetrics() {
        return EventUtils.threadReport(threadpool);
    }

    public ThreadPoolExecutor getThreadpool() {
        return threadpool;
    }
}
