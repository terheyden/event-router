package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Combines an event obj with an event key, for queuing and delivery.
 */
/*package*/  class PublishRequest {

    private final Object eventObj;
    private final Object eventKey;

    @Nullable
    private final UUID replyEventKey;

    /*package*/ PublishRequest(Object eventObj, Object eventKey, UUID replyEventKey) {
        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.replyEventKey = replyEventKey;
    }

    /*package*/ PublishRequest(Object eventObj, Object eventKey) {
        this.eventKey = eventKey;
        this.eventObj = eventObj;
        this.replyEventKey = null;
    }

    /*package*/ Object event() {
        return eventObj;
    }

    /*package*/ Object eventKey() {
        return eventKey;
    }

    /*package*/ Optional<UUID> replyEventKey() {
        return Optional.ofNullable(replyEventKey);
    }
}
