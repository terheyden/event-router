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

        return new PublishRequest<>(
            new EventRequest<>(event),
            new SequentialSendStrategy<>(EventRouters.DEFAULT_EXCEPTION_HANDLER),
            queue(subscription));
    }

    static Queue<EventSubscription> queue(EventSubscription subscription) {
        Queue<EventSubscription> queue = new ConcurrentLinkedQueue<>();
        queue.add(subscription);
        return queue;
    }
}
