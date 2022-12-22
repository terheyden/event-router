package com.terheyden.event;

import java.util.UUID;

import io.vavr.CheckedConsumer;

/**
 * SubscribeRequest class.
 */ /*package*/  class SubscribeRequest {

    private final UUID subscriptionId = UUID.randomUUID();
    private final EventKey eventKey;
    private final CheckedConsumer<?> eventHandler;

    public SubscribeRequest(EventKey eventKey, CheckedConsumer<?> eventHandler) {
        this.eventKey = eventKey;
        this.eventHandler = eventHandler;
    }

    public UUID subscriptionId() {
        return subscriptionId;
    }

    public EventKey eventKey() {
        return eventKey;
    }

    public CheckedConsumer<?> eventHandler() {
        return eventHandler;
    }
}
