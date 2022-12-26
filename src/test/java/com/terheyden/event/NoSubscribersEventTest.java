package com.terheyden.event;

import org.junit.jupiter.api.Test;

/**
 * NoSubscribersEventTest unit tests.
 */
public class NoSubscribersEventTest {

    @Test
    public void justForCoverage() {

        NoSubscribersEvent event = new NoSubscribersEvent("hello", String.class);
        event.event();
        event.eventType();
    }
}
