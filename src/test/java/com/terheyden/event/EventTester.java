package com.terheyden.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

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

    public static <T> List<T> publish(EventRouter router, Class<T> eventClass, T... events) {
        try {

            List<T> outputs = Collections.synchronizedList(new ArrayList<>(events.length));
            CountDownLatch outputLatch = new CountDownLatch(events.length);

            router.subscribe(eventClass, item -> {
                outputs.add(item);
                outputLatch.countDown();
                LOG.debug("Got event: {} ({} left)", item, outputLatch.getCount());
            });

            LOG.debug("Publishing {} events...", events.length);
            for (T event : events) {
                router.publish(event);
            }

            outputLatch.await();
            return outputs;

        } catch (InterruptedException e) {
            return EventUtils.throwUnchecked(e);
        }
    }

    public static void throwException(Object event) {
        throw new RuntimeException("Exception while processing event: " + event);
    }

    public static void awaitEmpty(ThreadPoolExecutor pool) {
        while (pool.getActiveCount() > 0) {
            TestUtils.sleep(100);
        }
    }
}
