package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
/*package*/  class PublishRequest {

    private final EventRouter eventRouter;
    private final Object eventObj;
    private final Object eventKey;
    private final EventPublisher eventPublisher;
    private final Queue<EventSubscription> subscribers;
    @Nullable
    private final CompletableFuture<Object> callbackFuture;

    /*package*/ PublishRequest(
        EventRouter eventRouter, Object eventObj,
        Object eventKey,
        EventPublisher eventPublisher,
        Queue<EventSubscription> subscribers,
        @Nullable CompletableFuture<Object> callbackFuture) {
        this.eventRouter = eventRouter;

        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.eventPublisher = eventPublisher;
        this.subscribers = subscribers;
        this.callbackFuture = callbackFuture;
    }

    /*package*/ PublishRequest(
        EventRouter eventRouter, Object eventObj,
        Object eventKey,
        EventPublisher eventPublisher,
        Queue<EventSubscription> subscribers) {
        this.eventRouter = eventRouter;

        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.eventPublisher = eventPublisher;
        this.subscribers = subscribers;
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

    /*package*/ Queue<EventSubscription> subscribers() {
        return subscribers;
    }

    /*package*/ EventRouter eventRouter() {
        return eventRouter;
    }
}
