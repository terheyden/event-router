package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
class ThreadPoolSendStrategy<T> implements SendEventStrategy<T> {

    private final ThreadPoolExecutor threadPool;

    public ThreadPoolSendStrategy(ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<? extends T> eventRequest, Collection<? extends EventSubscription> subscribers) {
        subscribers
            .stream()
            .map(sub -> (EventRouterSubscription<T>) sub)
            .forEach(sub ->
            threadPool.execute(() ->
                sub.getEventHandler().unchecked().accept(eventRequest.getEventObj())));
    }

    @Override
    public String getMetrics() {
        return EventUtils.threadReport(threadPool);
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}
