package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
/*package*/  class PublishRequest {

    private final Object eventObj;
    private final Object eventKey;
    private final EventPublisher eventPublisher;

    @Nullable
    private final CompletableFuture<Object> callbackFuture;

    /*package*/ PublishRequest(
        Object eventObj,
        Object eventKey,
        EventPublisher eventPublisher,
        @Nullable CompletableFuture<Object> callbackFuture) {

        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.eventPublisher = eventPublisher;
        this.callbackFuture = callbackFuture;
    }

    /*package*/ PublishRequest(
        Object eventObj,
        Object eventKey,
        EventPublisher eventPublisher) {

        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.eventPublisher = eventPublisher;
        this.callbackFuture = null;
    }

    /*package*/ Object event() {
        return eventObj;
    }

    /*package*/ Object eventKey() {
        return eventKey;
    }

    /*package*/ EventPublisher eventPublisher() {
        return eventPublisher;
    }

    /*package*/ Optional<CompletableFuture<Object>> callbackFuture() {
        return Optional.ofNullable(callbackFuture);
    }
}
