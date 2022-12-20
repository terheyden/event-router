package com.terheyden.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EventTester class.
 */
public final class EventTester {

    private EventTester() {
        // Private since this class shouldn't be instantiated.
    }

    public static <T> List<T> publish(EventRouter router, Class<T> eventClass, T... events) {

        List<T> outputs = Collections.synchronizedList(new ArrayList<>(events.length));

        router.subscribe(eventClass, outputs::add);

        for (T event : events) {
            router.publish(event);
        }

        return outputs;
    }

    public static void throwException(Object event) {
        throw new RuntimeException("Exception while processing event: " + event);
    }
}
