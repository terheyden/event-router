package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles incoming "sendEventToSubscribers my event" requests.
 * Publishing is always done asynchronously using a thread pool,
 * so thread death is automatically taken care of.
 */
class ReceivedEventHandler<T> {

    private static final Logger LOG = getLogger(ReceivedEventHandler.class);

    private final ThreadPoolExecutor eventRequestExecutor;

    ReceivedEventHandler(ThreadPoolExecutor eventRequestExecutor) {
        this.eventRequestExecutor = eventRequestExecutor;
    }

    /**
     * Our main entry point — the user calls EventRouter.sendEventToSubscribers() and EventRouter calls this.
     */
    void publish(PublishRequest<T> publishRequest) {
        eventRequestExecutor.execute(() -> processPublishRequest(publishRequest));
    }

    /**
     * Deliver the given sendEventToSubscribers request to subscribers.
     */
    private static <T> void processPublishRequest(PublishRequest<T> publishRequest) {

        Collection<EventSubscription> subscribers = publishRequest.subscribers();

        if (subscribers.isEmpty()) {
            LOG.trace("No subscribers for event: {}", publishRequest);
            return;
        }

        EventRequest<T> eventRequest = publishRequest.eventRequest();
        SendEventToSubscriberStrategy<T> sendStrategy = publishRequest.eventPublisher();
        LOG.trace("Dispatching event: {}", publishRequest);
        sendStrategy.sendEventToSubscribers(eventRequest, subscribers);
    }
}
