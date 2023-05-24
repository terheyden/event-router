package com.terheyden.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EventRoutersTest unit tests.
 */
public class EventRoutersTest {

    @Test
    public void testEventRouterImpl() {

        EventRouter<String> events = EventRouters
            .createWithEventType(String.class)
            .maxThreadPoolSize(1)
            .build();

        assertThat(events).isInstanceOf(EventRouterImpl.class);
    }

    @Test
    void testEventQueryImpl() {

        EventQuery<String, Integer> events = EventRouters
            .createWithEventType(String.class)
            .eventReplyType(Integer.class)
            .maxThreadPoolSize(1)
            .build();

        assertThat(events).isInstanceOf(EventQueryImpl.class);
    }

    @Test
    void testModifiableEventRouterImpl() {

        ModifiableEventRouter<String> events = EventRouters
            .createWithEventType(String.class)
            .modifiableEvents()
            .maxThreadPoolSize(1)
            .build();

        assertThat(events).isInstanceOf(ModifiableEventRouterImpl.class);
    }
}
