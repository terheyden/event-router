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

        // You can also send events that return a response object.
        // We call this an event query.
        // In this simple example, we'll publish a string event and expect the string length as a reply.
        EventQuery<String, Integer> stringLengthQuery = EventRouters
            .createWithEventType(String.class)
            .eventReplyType(Integer.class)
            .build();

        // Subscribe to the event, and calculate the string length as the response.
        stringLengthQuery.subscribe(String::length);

        // Publish a string event and specify the callback to call when the response is received.
        // Publishing and subscribing always happen asynchronously.
        stringLengthQuery.query(
            "Hello, world!",
            strLen -> System.out.println("String length: " + strLen));

        EventUtils.sleep(200);
    }

    @Test
    void tutorial3() {

        // The last kind of event you can send is a modifiable event.
        // Subscribers are given the event object in FIFO order,
        // and may update, replace, or event cancel the event by returning null.
    }
}
