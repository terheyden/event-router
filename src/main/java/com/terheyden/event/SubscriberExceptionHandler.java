package com.terheyden.event;

/**
 * Interface for handling exceptions thrown by subscribers during event publishing.
 */
@FunctionalInterface
public interface SubscriberExceptionHandler {

    /**
     * Handle an exception thrown by a subscriber while publishing an event.
     * @param thrownException The exception thrown by the subscriber.
     * @param eventObj The event object that caused the exception.
     */
    void handleException(Throwable thrownException, Object eventObj);
}
