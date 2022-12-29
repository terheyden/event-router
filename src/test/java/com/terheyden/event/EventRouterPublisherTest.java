package com.terheyden.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * EventRouterPublisherTest unit tests.
 */
public class EventRouterPublisherTest {

    private EventRouterPublisher publisher;

    @BeforeEach
    public void before() {
        publisher = new EventRouterPublisher(ThreadPools.newDynamicThreadPool(1));
    }

    @Test
    public void test() {

        StringBuilder builder = new StringBuilder();
        EventSubscription subscription = new EventSubscription(builder::append);
        PublishRequest request = Mocks.publishRequest(subscription, "hello");
        publisher.publish(request);
        EventUtils.sleep(300);
        assertEquals("hello", builder.toString());
    }
}
