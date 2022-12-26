package com.terheyden.event;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * QueryRequest class.
 */
public class QueryRequest extends PublishRequest {

    private final CompletableFuture<Object> callbackFuture;

    QueryRequest(
        EventRouter eventRouter,
        Object eventObj,
        Class<?> eventType,
        EventPublisher eventPublisher,
        Queue<EventSubscription> subscribers,
        CompletableFuture<Object> callbackFuture) {

        super(eventRouter, eventObj, eventType, eventPublisher, subscribers);
        this.callbackFuture = callbackFuture;
    }

    CompletableFuture<Object> callbackFuture() {
        return callbackFuture;
    }
}
