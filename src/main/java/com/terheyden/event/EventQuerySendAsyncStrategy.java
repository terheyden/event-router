package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
class EventQuerySendAsyncStrategy<I, O> extends ExceptionHandlingSendEventStrategy<I> {

    private final ThreadPoolExecutor threadPool;

    EventQuerySendAsyncStrategy(SubscriberExceptionHandler exceptionHandler, ThreadPoolExecutor threadPool) {
        super(exceptionHandler);
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
            threadPool.execute(() -> sendEventToSubscriber(sub, eventRequest)));
    }

    private void sendEventToSubscriber(EventQuerySubscription<I, O> sub, EventRequest<? extends I> eventRequest) {
        try {
            SendStrategies.sendQueryEventResponse(eventRequest, sub);
        } catch (Exception e) {
            handleException(e, eventRequest);
        }
    }
}
