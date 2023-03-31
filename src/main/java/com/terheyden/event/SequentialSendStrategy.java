package com.terheyden.event;

import java.util.Queue;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Publishes events to subscribers in order, on the calling thread.
 */
public class SequentialSendStrategy implements SendEventToSubscriberStrategy {

    private static final Logger LOG = getLogger(SequentialSendStrategy.class);

    @Override
    public void sendEventToSubscribers(Object event, Queue<EventSubscription> subscribers) {

        subscribers.forEach(sub ->
            sub.getEventHandler().unchecked().accept(event));
    }

    @Override
    public String getMetrics() {
        return "SequentialSendStrategy has no metrics.";
    }
}
