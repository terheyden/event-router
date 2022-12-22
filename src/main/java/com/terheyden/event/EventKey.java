package com.terheyden.event;

import java.util.UUID;

/**
 * EventKey enum.
 */
public class EventKey {

    private final EventKeyType eventKeyType;
    private final Object key;

    public EventKey(Class<?> classKey) {
        this.eventKeyType = EventKeyType.CLASS;
        this.key = classKey;
    }

    public EventKey(UUID uuidKey) {
        this.eventKeyType = EventKeyType.UID;
        this.key = uuidKey;
    }

    public EventKeyType eventKeyType() {
        return eventKeyType;
    }

    public Class<?> classKey() {
        return (Class<?>) key;
    }

    public UUID uuidKey() {
        return (UUID) key;
    }
}
