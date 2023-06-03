package com.terheyden.event;

/**
 * Exception handling should be defined along with the router.
 * It needs to be enforced at the individual subscriber level,
 * so that one misbehaving subscriber doesn't impact everyone else.
 * Therefore we want to pass the exception handling down to the senders.
 */
abstract class ExceptionHandlingSendEventStrategy<T> implements SendEventStrategy<T> {

    private final SubscriberExceptionHandler exceptionHandler;

    protected ExceptionHandlingSendEventStrategy(SubscriberExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Handle an exception thrown by a subscriber while publishing an event.
     * @param thrownException The exception thrown by the subscriber.
     * @param eventRequest The event object that caused the exception.
     */
    protected void handleException(Throwable thrownException, EventRequest<? extends T> eventRequest) {
        exceptionHandler.handleException(thrownException, eventRequest.getEventObj());
    }

    protected SubscriberExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
}
