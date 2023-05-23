package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
public class ThreadPoolSendStrategy<T> implements SendEventToSubscriberStrategy<T> {

    private final ThreadPoolExecutor threadpool;

    public ThreadPoolSendStrategy(ThreadPoolExecutor threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<T> eventRequest, Collection<EventSubscription> subscribers) {
        subscribers
            .stream()
            .map(sub -> (EventRouterSubscription<T>) sub)
            .forEach(sub ->
            threadpool.execute(() ->
                sub.getEventHandler().unchecked().accept(eventRequest.getEventObj())));
    }

    @Override
    public String getMetrics() {
        return EventUtils.threadReport(threadpool);
    }

    public ThreadPoolExecutor getThreadpool() {
        return threadpool;
    }
}
