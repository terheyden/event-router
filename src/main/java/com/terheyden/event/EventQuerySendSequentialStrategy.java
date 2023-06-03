package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
class EventQuerySendSequentialStrategy<I, O> extends ExceptionHandlingSendEventStrategy<I> {

    EventQuerySendSequentialStrategy(SubscriberExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(
        EventRequest<? extends I> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

        subscribers
            .stream()
            .map(sub -> (EventQuerySubscription<I, O>) sub)
            .forEach(sub -> SendStrategies.sendQueryEventResponse(eventRequest, sub));
    }
}
