package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles incoming "sendEventToSubscribers my event" requests.
 * Publishing is always done asynchronously using a thread pool,
 * so thread death is automatically taken care of.
 */
class ReceivedEventHandler {

    private static final Logger LOG = getLogger(ReceivedEventHandler.class);

    private final ThreadPoolExecutor eventRequestExecutor;

    ReceivedEventHandler(ThreadPoolExecutor eventRequestExecutor) {
        this.eventRequestExecutor = eventRequestExecutor;
    }

    /**
     * Our main entry point — the user calls EventRouter.sendEventToSubscribers() and EventRouter calls this.
     */
    void publish(PublishRequest publishRequest) {
        eventRequestExecutor.execute(() -> processPublishRequest(publishRequest));
    }

    /**
     * Deliver the given sendEventToSubscribers request to subscribers.
     */
    private static void processPublishRequest(PublishRequest publishRequest) {

        Queue<EventSubscription> subscribers = publishRequest.subscribers();

        if (subscribers.isEmpty()) {
            LOG.trace("No subscribers for event: {}", publishRequest);
            handleNoSubscribersEvent(publishRequest);
            return;
        }

        Object event = publishRequest.event();
        SendEventToSubscriberStrategy sendStrategy = publishRequest.eventPublisher();
        LOG.trace("Dispatching event: {}", publishRequest);
        sendStrategy.sendEventToSubscribers(event, subscribers);
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
