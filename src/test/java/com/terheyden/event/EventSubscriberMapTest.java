package com.terheyden.event;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EventSubscriberMapTest unit tests.
 */
public class EventSubscriberMapTest {

    private EventSubscriberMap map;
    private AtomicInteger counter;

    @BeforeEach
    public void beforeEach() {
        map = new EventSubscriberMap();
        counter = new AtomicInteger(0);
    }

    @Test
    public void testRemoveSubscription() {
        UUID uuid = map.add(String.class, str -> counter.incrementAndGet());
        assertTrue(map.remove(String.class, uuid));
        assertFalse(map.remove(String.class, uuid));
    }
}
