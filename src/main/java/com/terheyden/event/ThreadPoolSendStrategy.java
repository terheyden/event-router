package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
class ThreadPoolSendStrategy<T> extends ExceptionHandlingSendEventStrategy<T> {

    private final ThreadPoolExecutor threadPool;

    ThreadPoolSendStrategy(SubscriberExceptionHandler exceptionHandler, ThreadPoolExecutor threadPool) {
        super(exceptionHandler);
        this.threadPool = threadPool;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<? extends T> eventRequest, Collection<? extends EventSubscription> subscribers) {
        subscribers
            .stream()
            .map(sub -> (EventRouterSubscription<T>) sub)
            .forEach(sub ->
            threadPool.execute(() -> sendEventToSubscriber(sub, eventRequest)));
    }

    private void sendEventToSubscriber(EventRouterSubscription<T> sub, EventRequest<? extends T> eventRequest) {
        try {
            sub.getEventHandler().unchecked().accept(eventRequest.getEventObj());
        } catch (Exception e) {
            handleException(e, eventRequest);
        }
    }
}
