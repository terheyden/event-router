package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles publish requests.
 * Publishing is always done asynchronously using a thread pool,
 * so thread death is automatically taken care of.
 */
class EventRouterPublisher {

    private static final Logger LOG = getLogger(EventRouterPublisher.class);

    private final ThreadPoolExecutor publishRequestExecutor;

    EventRouterPublisher(ThreadPoolExecutor publishRequestExecutor) {
        this.publishRequestExecutor = publishRequestExecutor;
    }

    void publish(PublishRequest publishRequest) {
        publishRequestExecutor.execute(() -> processPublishRequest(publishRequest));
    }

    /**
     * Deliver the given publish request to subscribers.
     */
    private static void processPublishRequest(PublishRequest publishRequest) {

        Queue<EventSubscription> subscribers = publishRequest.subscribers();

        if (subscribers.isEmpty()) {
            LOG.trace("No subscribers for event: {}", publishRequest);
            handleNoSubscribersEvent(publishRequest);
            return;
        }

        Object event = publishRequest.event();
        EventPublisher publisher = publishRequest.eventPublisher();
        LOG.trace("Dispatching event: {}", publishRequest);
        publisher.publish(event, subscribers);
    }

    /**
     * If it's a user event with no subscribers, send a {@link NoSubscribersEvent}.
     */
    private static void handleNoSubscribersEvent(PublishRequest publishRequest) {

        Class<?> eventType = publishRequest.eventType();
        // https://stackoverflow.com/questions/12145185/determine-if-a-class-implements-a-interface-in-java
        boolean internalEvent = SpecialEvent.class.isAssignableFrom(eventType);

        if (internalEvent) {
            // Avoid infinite loop.
            return;
        }

        Object event = publishRequest.event();
        EventRouter eventRouter = publishRequest.eventRouter();
        LOG.debug("No subscribers for event: {} ({})", eventType, event);
        eventRouter.publish(new NoSubscribersEvent(event, eventType));
    }

    /**
     * Access to the publish pool, for metrics.
     */
    ThreadPoolExecutor getPublishRequestExecutor() {
        return publishRequestExecutor;
    }
}
