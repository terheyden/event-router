package com.terheyden.event;

/**
 * This event is published when there are no subscribers for an event.
 */
public class NoSubscribersEvent implements SpecialEvent {

    private final Object eventObj;
    private final Class<?> eventType;

    public NoSubscribersEvent(Object eventObj, Class<?> eventType) {
        this.eventObj = eventObj;
        this.eventType = eventType;
    }

    public Object event() {
        return eventObj;
    }

    public Class<?> eventType() {
        return eventType;
    }
}
