package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers in order, on the calling thread.
 * Subscribers may modify the event object, replace it completely, or return null to stop propagation.
 */
class ModifiableEventSendStrategy<T> extends ExceptionHandlingSendEventStrategy<T> {

    ModifiableEventSendStrategy(SubscriberExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    public void sendEventToSubscribers(
        EventRequest<? extends T> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

        SendStrategies.sendModifiableEventToSubscribers(eventRequest, subscribers, getExceptionHandler());
    }
}
