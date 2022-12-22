package com.terheyden.event;

/**
 * This event is published when there are no subscribers for an event.
 */
public class NoSubscribersEvent implements SpecialEvent {

    private final Object eventObj;
    private final Class<?> eventClass;

    public NoSubscribersEvent(Object eventObj, Class<?> eventClass) {
        this.eventObj = eventObj;
        this.eventClass = eventClass;
    }

    public Object event() {
        return eventObj;
    }

    public Class<?> eventClass() {
        return eventClass;
    }
}
