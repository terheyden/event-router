package com.terheyden.event;

import org.junit.jupiter.api.Test;

/**
 * TutorialTest class.
 */
class TutorialTest {

    @Test
    void tutorial1() {

        // Let's make a simple event router.
        // String events will get sent, and when they are received they'll just print to stdout.
        EventRouter<String> printStringEvent = EventRouters
            .createWithEventType(String.class)
            .build();

        // Let's subscribe to all String events.
        printStringEvent.subscribe(str -> System.out.println("Received: " + str));

        // Publish a String event to test.
        printStringEvent.publish("Hello, world!");

        // Everything happens asynchronously, so wait a sec for the event to be processed.
        EventUtils.sleep(200);
    }
}
