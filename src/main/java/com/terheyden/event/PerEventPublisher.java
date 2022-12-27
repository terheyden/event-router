package com.terheyden.event;

import java.util.Queue;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
public class PerEventPublisher implements EventPublisher {

    @Override
    public void publish(Object event, Queue<EventSubscription> subscribers) {
        subscribers.forEach(sub ->
            sub.getEventHandler().unchecked().accept(event));
    }
}
