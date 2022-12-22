package com.terheyden.event;

import java.util.UUID;

/**
 * UnsubscribeRequest class.
 */ /*package*/  class UnsubscribeRequest {

    private final EventKey eventKey;
    private final UUID subscriptionId;

    public UnsubscribeRequest(EventKey eventKey, UUID subscriptionId) {
        this.eventKey = eventKey;
        this.subscriptionId = subscriptionId;
    }

    public EventKey eventKey() {
        return eventKey;
    }

    public UUID subscriptionId() {
        return subscriptionId;
    }
}
