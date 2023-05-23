package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
public class SequentialSendStrategy<T> implements SendEventToSubscriberStrategy<T> {

    @Override
    public void sendEventToSubscribers(T event, Collection<EventSubscription<T>> subscribers) {

        subscribers.forEach(sub ->
            sub.getEventHandler().unchecked().accept(event));
    }

    @Override
    public String getMetrics() {
        return "SequentialSendStrategy has no metrics.";
    }
}
