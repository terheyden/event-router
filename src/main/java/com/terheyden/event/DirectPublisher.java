package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers "directly," meaning, on the calling thread.
 */
public class DirectPublisher implements EventPublisher {

    @Override
    public void publish(Object event, Collection<EventSubscription> subscribers) {
        subscribers.forEach(sub -> sub.getEventHandler().unchecked().accept(event));
    }
}
