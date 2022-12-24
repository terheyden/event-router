package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes events to subscribers "directly," meaning, on the calling thread.
 */
public class DirectPublisher implements EventPublisher {

    @Override
    public void publish(Object event, Collection<EventSubscription> subscribers) {
        subscribers.forEach(sub -> sub.getEventHandler().unchecked().apply(event));
    }

    @Override
    public void query(
        Object event,
        Collection<EventSubscription> subscribers,
        CompletableFuture<Object> callbackFuture) {

        subscribers.forEach(sub -> {
            Object result = sub.getEventHandler().unchecked().apply(event);
            callbackFuture.complete(result);
        });
    }
}
