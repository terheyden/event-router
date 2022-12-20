package com.terheyden.event;

import java.util.List;

import org.slf4j.Logger;

import io.vavr.CheckedConsumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This layer's entire job is just to provide helper methods to subclasses.
 */
public abstract class BasePublisher implements EventPublisher {

    private static final Logger LOG = getLogger(BasePublisher.class);

    /**
     * Publishers can call this to deliver an event to a single subscriber.
     * Exception handling is taken care of; exceptions are caught and converted into a new separate event.
     */
    protected void publishChecked(EventRouter sourceRouter, EventSubscription subscription, Object event) {
        try {

            CheckedConsumer<Object> eventHandler = subscription.getEventHandler();
            eventHandler.accept(event);

        } catch (Throwable throwable) {

            // If an exception is thrown while handling an exception event,
            // we don't want to get stuck in an infinite loop, so log it.
            if (event instanceof EventRouterPublishException) {
                LOG.error("Exception while handling an exception event.", throwable);
                return;
            }

            // Else publish the exception event so the caller can process it however they wish.
            LOG.debug("Sending exception as a new event: {}", throwable.getMessage());
            EventRouterPublishException exception = new EventRouterPublishException(
                subscription,
                event,
                "Exception while publishing event.",
                throwable);

            // The source router will send the exception as an event.
            sourceRouter.publish(exception);
        }
    }

    /**
     * Publishers can call this to deliver an event, in-order, to a list of subscribers.
     */
    protected void publishAllChecked(EventRouter sourceRouter, List<EventSubscription> subscriptions, Object event) {
        for (EventSubscription subscription : subscriptions) {
            publishChecked(sourceRouter, subscription, event);
        }
    }
}
