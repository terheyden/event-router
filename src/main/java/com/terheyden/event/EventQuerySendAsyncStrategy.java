package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
class EventQuerySendAsyncStrategy<I, O> implements SendEventStrategy<I> {

    private final ThreadPoolExecutor threadPool;

    EventQuerySendAsyncStrategy(ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(
        EventRequest<? extends I> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

        subscribers
            .stream()
            .map(sub -> (EventQuerySubscription<I, O>) sub)
            .forEach(sub ->
            threadPool.execute(() -> SendStrategies.sendQueryEventResponse(eventRequest, sub)));
    }

    @Override
    public String getMetrics() {
        return EventUtils.threadReport(threadPool);
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}
