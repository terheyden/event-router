package com.terheyden.event;

import java.util.Collection;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
class SequentialSendStrategy<T> extends ExceptionHandlingSendEventStrategy<T> {

    private static final Logger LOG = getLogger(SequentialSendStrategy.class);

    SequentialSendStrategy(SubscriberExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(
        EventRequest<? extends T> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

        subscribers
            .stream()
            .map(sub -> (EventRouterSubscription<T>) sub)
            .forEach(sub -> sendEventToSubscriber(sub, eventRequest));
    }

    private void sendEventToSubscriber(EventRouterSubscription<T> sub, EventRequest<? extends T> eventRequest) {
        try {
            sub.getEventHandler().unchecked().accept(eventRequest.getEventObj());
        } catch (Exception e) {
            handleException(e, eventRequest);
        }
    }
}
