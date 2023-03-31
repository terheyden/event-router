package com.terheyden.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ReceivedEventHandlerTest unit tests.
 */
public class ReceivedEventHandlerTest {

    private ReceivedEventHandler publisher;

    @BeforeEach
    public void before() {
        publisher = new ReceivedEventHandler(ThreadPools.newDynamicThreadPool(1));
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
