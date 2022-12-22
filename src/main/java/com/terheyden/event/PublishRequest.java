package com.terheyden.event;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
/*package*/  class PublishRequest {

    private final Object eventObj;
    private final Object eventKey;

    /*package*/ PublishRequest(Object eventObj, Object eventKey) {
        this.eventKey = eventKey;
        this.eventObj = eventObj;
    }

    public Object event() {
        return eventObj;
    }

    public Object eventKey() {
        return eventKey;
    }
}
