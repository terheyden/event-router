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
class EventRouterPublisher {

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
    void publish(PublishRequest publishRequest) {
        // First, put the event on the queue.
        publishRequests.add(publishRequest);
        // Then, process the queue.
        processAllPublishRequests();
    }

    void query(PublishRequest publishRequest) {

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

        // Sending subscribers as a second param instead of letting the methods pull it out
        // since we have added value by validating.
        if (publishRequest instanceof QueryRequest) {
            sendQueryRequest((QueryRequest) publishRequest, subscribers);
        } else {
            sendPublishRequest(publishRequest, subscribers);
        }
    }

    private static void sendPublishRequest(
        PublishRequest publishRequest,
        Queue<EventSubscription> subscribers) {

        Object event = publishRequest.event();
        Class<?> eventType = publishRequest.eventType();
        LOG.debug("Publishing event type '{}' to {} subscribers.", eventType, subscribers.size());

        EventPublisher publisher = publishRequest.eventPublisher();
        publisher.publish(event, subscribers);
    }

    private static void sendQueryRequest(QueryRequest queryRequest, Queue<EventSubscription> subscribers) {

        Object event = queryRequest.event();
        Class<?> eventType = queryRequest.eventType();
        LOG.debug("Query event type '{}' to {} subscribers.", eventType, subscribers.size());

        EventPublisher publisher = queryRequest.eventPublisher();
        CompletableFuture<Object> callbackFuture = queryRequest.callbackFuture();
        publisher.query(event, subscribers, callbackFuture);
    }

    /**
     * If it's a user event with no subscribers, send a {@link NoSubscribersEvent}.
     */
    private static void handleNoSubscribersEvent(PublishRequest request) {

        Class<?> eventType = request.eventType();
        // https://stackoverflow.com/questions/12145185/determine-if-a-class-implements-a-interface-in-java
        boolean internalEvent = SpecialEvent.class.isAssignableFrom(eventType);

        if (internalEvent) {
            // Avoid infinite loop.
            return;
        }

        Object event = request.event();
        EventRouter eventRouter = request.eventRouter();
        LOG.debug("No subscribers for event: {}", event);
        eventRouter.publish(new NoSubscribersEvent(event, eventType));
    }
}
