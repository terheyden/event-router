package com.terheyden.event;

import java.util.UUID;
import java.util.function.Function;

/**
 * EventKey enum.
 */
public enum EventKeyType {
    CLASS(Class.class, clazz -> ((Class<?>) clazz).getSimpleName()),
    UID(UUID.class, uuid -> ((UUID) uuid).toString());

    private final Class<?> keyClass;
    private final Function<Object, String> toStringFunc;

    EventKeyType(Class<?> keyClass, Function<Object, String> toStringFunc) {
        this.keyClass = keyClass;
        this.toStringFunc = toStringFunc;
    }

    public String getStringValue(Object key) {
        return toStringFunc.apply(key);
    }
}
