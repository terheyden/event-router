package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles publish and query requests.
 */
/*package*/ class EventRouterPublisher {

    private static final Logger LOG = getLogger(EventRouterPublisher.class);

    /**
     * Events should be processed in-order. If publishing one event causes another,
     * at least wait until the current event is finished.
     */
    private final Queue<PublishRequest> publishRequests = new ConcurrentLinkedQueue<>();

    /**
     * Publish requests should finish in-order.
     * To enforce this, we use a lock.
     */
    private final Semaphore publishLock = new Semaphore(1);

    /**
     * Internal publish that works with UUIDs.
     */
    /*package*/ void publishInternal(PublishRequest publishRequest) {
        // First, put the event on the queue.
        publishRequests.add(publishRequest);
        // Then, process the queue.
        processAllPublishRequests();
    }

    /*package*/ void query(PublishRequest publishRequest) {

        // Put the query on the queue.
        publishRequests.add(publishRequest);
        // Then, process the queue.
        processAllPublishRequests();
    }

    /**
     * Take from the publish request queue and deliver to subscribers
     * until the queue is empty again.
     */
    private void processAllPublishRequests() {

        // If someone's already processing the queue, don't do it again.
        // This prevents the same thread from being interrupted by a second publish.
        if (!publishLock.tryAcquire()) {
            return;
        }

        // Put everything in a try so we can guarantee the lock will be released.
        try {

            @Nullable PublishRequest publishRequest = publishRequests.poll();

            while (publishRequest != null) {

                processPublishRequest(publishRequest);
                // Get the next event.
                publishRequest = publishRequests.poll();
            }

        } finally {
            publishLock.release();
        }
    }

    /**
     * Deliver the given publish request to subscribers.
     */
    private static void processPublishRequest(PublishRequest publishRequest) {

        Queue<EventSubscription> subscribers = publishRequest.subscribers();

        if (subscribers.isEmpty()) {
            handleNoSubscribersEvent(publishRequest);
            return;
        }

        Object event = publishRequest.event();
        Object eventKey = publishRequest.eventKey();
        LOG.debug(
            "Publishing event type '{}' to {} subscribers.",
            eventKey.toString(),
            subscribers.size());

        EventPublisher publisher = publishRequest.eventPublisher();

        if (publishRequest.callbackFuture().isPresent()) {
            // This is a query.
            CompletableFuture<Object> callbackFuture = publishRequest.callbackFuture().get();
            publisher.query(event, subscribers, callbackFuture);
        } else {
            // This is a regular publish.
            publisher.publish(event, subscribers);
        }
    }

    /**
     * If it's a user event with no subscribers, send a {@link NoSubscribersEvent}.
     */
    private static void handleNoSubscribersEvent(PublishRequest request) {

        Object eventKey = request.eventKey();
        // If it's not a class, it's an internal event.
        if (!(eventKey instanceof Class)) {
            return;
        }

        Class<?> eventClassKey = (Class<?>) eventKey;
        // https://stackoverflow.com/questions/12145185/determine-if-a-class-implements-a-interface-in-java
        boolean internalEvent = SpecialEvent.class.isAssignableFrom(eventClassKey);

        if (internalEvent) {
            // Avoid infinite loop.
            return;
        }

        Object event = request.event();
        EventRouter eventRouter = request.eventRouter();
        LOG.debug("No subscribers for event: {}", event);
        eventRouter.publish(new NoSubscribersEvent(event, eventClassKey));
    }
}
