package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Mocks class.
 */
public final class Mocks {

    private Mocks() {
        // Private since this class shouldn't be instantiated.
    }

    static PublishRequest<String> publishRequest(EventSubscription subscription, String event) {

        PublishRequest<String> request = new PublishRequest<>(
            new EventRequest<>(event),
            new SequentialSendStrategy<>(),
            queue(subscription));

        return request;
    }

    static Queue<EventSubscription> queue(EventSubscription subscription) {
        Queue<EventSubscription> queue = new ConcurrentLinkedQueue<>();
        queue.add(subscription);
        return queue;
    }
}
