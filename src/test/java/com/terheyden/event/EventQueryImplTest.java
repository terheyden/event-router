package com.terheyden.event;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventQueryImplTest unit tests.
 */
class EventQueryImplTest {

    private static final Logger LOG = getLogger(EventQueryImplTest.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    @Test
    void test() throws InterruptedException {

        // Query sends a string, and gets the string length back...
        EventQuery<String, Integer> events = EventRouters
            .createWithEventType(String.class)
            .eventReplyType(Integer.class)
            .build();

        events.subscribe(String::length);
        LOG.debug("Sending query...");
        events.query("hello", this::verifyQueryResult);

        LOG.debug("Waiting for query result...");
        latch.await();
    }

    // Called back by the event query responder.
    private void verifyQueryResult(int strLen) {
        LOG.debug("Got query result: {}", strLen);
        assertThat(strLen).isEqualTo(5);
        latch.countDown();
    }
}
