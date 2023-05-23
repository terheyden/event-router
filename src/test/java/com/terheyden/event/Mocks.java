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

    static EventRouter eventRouter() {
        return new EventRouterImpl<>(1);
    }

    static PublishRequest publishRequest(EventSubscription subscription, Object event) {

        PublishRequest request = new PublishRequest(
            eventRouter(),
            event,
            new SequentialSendStrategy(),
            queue(subscription));

        return request;
    }

    static Queue<EventSubscription> queue(EventSubscription subscription) {
        Queue<EventSubscription> queue = new ConcurrentLinkedQueue<>();
        queue.add(subscription);
        return queue;
    }
}
