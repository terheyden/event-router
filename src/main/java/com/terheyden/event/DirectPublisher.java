package com.terheyden.event;

import java.util.List;

/**
 * Publishes events to subscribers "directly," meaning, on the calling thread.
 */
public class DirectPublisher extends BasePublisher {

    @Override
    public void publish(EventRouter sourceRouter, Object event, List<EventSubscription> subscribers) {

        for (EventSubscription subscriber : subscribers) {
            publishChecked(sourceRouter, subscriber, event);
        }
    }
}
