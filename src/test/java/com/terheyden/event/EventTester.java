package com.terheyden.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventTester class.
 */
public final class EventTester {

    private static final Logger LOG = getLogger(EventTester.class);

    private EventTester() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Publish the given events to the specified router.
     * Block (for 3 seconds) until all events to go through.
     *
     * @return a list of all events that were published and received
     */
    public static <T> List<T> publish(EventRouter<T> router, T... events) {
        try {

            List<T> outputs = Collections.synchronizedList(new ArrayList<>(events.length));
            CountDownLatch outputLatch = new CountDownLatch(events.length);

            router.subscribe(item -> {
                outputs.add(item);
                outputLatch.countDown();
                LOG.debug("Got event: {} ({} left)", item, outputLatch.getCount());
            });

            LOG.debug("Publishing {} events...", events.length);
            for (T event : events) {
                router.publish(event);
            }

            outputLatch.await(3, TimeUnit.SECONDS);
            return outputs;

        } catch (InterruptedException e) {
            return EventUtils.throwUnchecked(e);
        }
    }

    public static void throwException(Object event) {
        throw new RuntimeException("Exception while processing event: " + event);
    }

    /**
     * Waits for the event router to have no more active threads.
     */
    public static void awaitEmpty(EventSubscriber eventRouter) {
        while (eventRouter.getThreadPool().getActiveCount() > 0) {
            EventUtils.sleep(100);
        }
    }
}
