package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;

/**
 * Publishes events to subscribers "directly," meaning, on the calling thread.
 */
public class DirectPublisher implements EventPublisher {

    @Override
    public void publish(EventRouter sourceRouter, Object event, Collection<EventSubscription> subscribers) {
        subscribers.forEach(sub -> sub.getEventHandler().unchecked().apply(event));
    }

    @Override
    public void query(
        EventRouter sourceRouter,
        Object event,
        Collection<EventSubscription> subscribers,
        UUID callbackEventKey) {

        subscribers.forEach(sub -> {
            Object result = sub.getEventHandler().unchecked().apply(event);
            sourceRouter.publishInternal(result, callbackEventKey);
        });
    }
}
