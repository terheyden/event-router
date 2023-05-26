package com.terheyden.event;

import java.util.Collection;

/**
 * Publishes events to subscribers in order, on the calling thread.
 * Subscribers may modify the event object, replace it completely, or return null to stop propagation.
 */
class ModifiableEventSendStrategy<T> implements SendEventStrategy<T> {

    @Override
    public void sendEventToSubscribers(
        EventRequest<? extends T> eventRequest,
        Collection<? extends EventSubscription> subscribers) {

        SendStrategies.sendModifiableEventToSubscribers(eventRequest, subscribers);
    }

    @Override
    public String getMetrics() {
        return "ModifiableEventSendStrategy has no metrics.";
    }
}
