package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Collection;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Publishes events to subscribers in order, on the calling thread.
 * Subscribers may modify the event object, replace it completely, or return null to stop propagation.
 */
class ModifiableEventSendStrategy<T> implements SendEventToSubscriberStrategy<T> {

    private static final Logger LOG = getLogger(ModifiableEventSendStrategy.class);

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<T> eventRequest, Collection<EventSubscription> subscribers) {

        @Nullable T eventObj = eventRequest.getEventObj();

        // The event is passed to subscribers in order.
        // Each subscriber may modify the event obj, replace it completely, or return null.
        // Returning null is a signal to stop propagating the event.
        for (EventSubscription subscriber : subscribers) {

            // If anyone returns null, stop propagating the event.
            if (eventObj == null) {
                LOG.debug("Event is now null; stopping propagation.");
                break;
            }

            ModifiableEventSubscription<T> sub = (ModifiableEventSubscription<T>) subscriber;
            eventObj = sub.getEventHandler().unchecked().apply(eventObj);
        }
    }

    @Override
    public String getMetrics() {
        return "ModifiableEventSendStrategy has no metrics.";
    }
}
