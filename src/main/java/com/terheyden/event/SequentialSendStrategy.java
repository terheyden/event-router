package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
class SequentialSendStrategy<T> implements SendEventStrategy<T> {

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<? extends T> eventRequest, Collection<? extends EventSubscription> subscribers) {

        subscribers
            .stream()
            .map(sub -> (EventRouterSubscription<T>) sub)
            .forEach(sub ->
            sub.getEventHandler().unchecked().accept(eventRequest.getEventObj()));
    }

    @Override
    public String getMetrics() {
        return "SequentialSendStrategy has no metrics.";
    }
}
