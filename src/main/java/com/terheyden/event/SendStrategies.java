package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Collection;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * SendStrategies class.
 */
final class SendStrategies {

    private static final Logger LOG = getLogger(SendStrategies.class);

    private SendStrategies() {
        // Private since this class shouldn't be instantiated.
    }

    static <I, O> void sendQueryEventResponse(
        EventRequest<I> eventRequest,
        EventQuerySubscription<? super I, O> sub) {

        QueryEventRequest<I, O> queryRequest = (QueryEventRequest<I, O>) eventRequest;

        LOG.debug("Sending event query to subscriber.");
        O queryResponse = sub.getEventHandler().unchecked().apply(queryRequest.getEventObj());

        LOG.debug("Sending query response back to caller: {}", queryResponse);
        queryRequest.getCallback().unchecked().accept(queryResponse);
    }

    @SuppressWarnings("unchecked")
    static <T> void sendModifiableEventToSubscribers(
        EventRequest<T> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

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
}
