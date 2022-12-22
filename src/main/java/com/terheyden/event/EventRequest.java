package com.terheyden.event;

/**
 * Combines an event with its class type, for queuing and delivery.
 */
/*package*/  class EventRequest {

    private final Object event;
    private final Object uuidClass;

    public EventRequest(Object event, Object uuidClass) {
        this.event = event;
        this.uuidClass = uuidClass;
    }

    public Object event() {
        return event;
    }

    public Object uuidClass() {
        return uuidClass;
    }
}
