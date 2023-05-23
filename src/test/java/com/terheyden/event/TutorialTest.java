package com.terheyden.event;

import org.junit.jupiter.api.Test;

/**
 * TutorialTest class.
 */
public class TutorialTest {

    @Test
    public void tutorial1() {

        // Let's make a simple event router.
        EventRouter<String> eventRouter = new EventRouterImpl<String>();

        // Let's subscribe to all String events.
        eventRouter.subscribe(str -> System.out.println("Received: " + str));

        // Sweet, now let's sendEventToSubscribers our first event.
        eventRouter.publish("Hello, world!");
    }
}
