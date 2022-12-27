package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles publish and query requests.
 */
class EventRouterPublisher {

    private static final Logger LOG = getLogger(EventRouterPublisher.class);

    /**
     * Blocking queue so threads will hang out until messages arrive.
     * Threads in the middle of publishing should not interrupt themselves
     * if, e.g., the running threads fires another event.
     * Use: put() and take().
     */
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
            handleNoSubscribersEvent(publishRequest);
            return;
        }

        Object event = publishRequest.event();
        Class<?> eventType = publishRequest.eventType();
        LOG.debug("Publishing event type '{}' to {} subscribers.", eventType, subscribers.size());

        EventPublisher publisher = publishRequest.eventPublisher();
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
}
