package com.terheyden.event;

import java.util.StringJoiner;

/**
 * For events and related data as they trigger subscribers.
 */
class EventRequest<T> {

    private final T eventObj;

    EventRequest(T eventObj) {
        this.eventObj = eventObj;
    }

    T getEventObj() {
        return eventObj;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EventRequest.class.getSimpleName() + "[", "]")
            .add("eventObj=" + eventObj)
            .toString();
    }
}
