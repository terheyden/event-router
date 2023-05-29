package com.terheyden.event;

import org.junit.jupiter.api.Test;

/**
 * TutorialTest class.
 */
class TutorialTest {

    @Test
    void tutorial1() {

        // Let's make a simple "Hello World" event router.
        // String events will get sent, and when they are received they'll just print to stdout.
        EventRouter<String> printStringEvent = EventRouters
            .createWithEventType(String.class)
            .build();

        printStringEvent.subscribe(str -> System.out.println("Received: " + str));

        // Publish a String event to test.
        printStringEvent.publish("Hello, world!");

        // Everything happens asynchronously, so wait a sec for the event to be processed.
        EventUtils.sleep(200);
    }

    @Test
    void tutorial2() {

        // By specifying an event reply type, we can create a special type of event that
        // expects a response from the subscriber.
        // In this simple example, we'll publish a string event and expect the string length as a reply.
        EventQuery<String, Integer> stringLengthQuery = EventRouters
            .createWithEventType(String.class)
            .eventReplyType(Integer.class)
            .build();

        // Subscribe to the string length query event.
        stringLengthQuery.subscribe(String::length);

        // Publish a string event and expect a reply.
        stringLengthQuery.query(
            "Hello, world!",
            strLen -> System.out.println("String length: " + strLen));
    }
}
