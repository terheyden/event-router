package com.terheyden.event;

/**
 * QueryEventRequest class.
 */
class QueryEventRequest<I, O> extends EventRequest<I> {

    private final CheckedConsumer<O> callback;

    QueryEventRequest(I eventObj, CheckedConsumer<O> callback) {
        super(eventObj);
        this.callback = callback;
    }

    CheckedConsumer<O> getCallback() {
        return callback;
    }
}
