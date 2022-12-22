package com.terheyden.event;

import org.junit.jupiter.api.Test;

/**
 * TutorialTest class.
 */
public class TutorialTest {

    @Test
    public void tutorial1() {

        // Let's make a simple event router.
        EventRouter eventRouter = new EventRouter();

        // Let's subscribe to all String events.
        eventRouter.subscribe(String.class, str -> System.out.println("Received: " + str));

        // Sweet, now let's publish our first event.
        eventRouter.publish("Hello, world!");
    }
}
