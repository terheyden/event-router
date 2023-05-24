package com.terheyden.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.vavr.CheckedRunnable;

import static java.lang.System.out;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * ThreadPoolTest unit tests.
 */
public class ThreadPoolTest {

    private static final Logger LOG = getLogger(ThreadPoolTest.class);

    private EventRouter<Fruit> router;
    private List<Integer> results;

    @BeforeEach
    public void beforeEach() {

        router = EventRouters.createWithEventType(Fruit.class).build();
        results = Collections.synchronizedList(new ArrayList<>());

        router.subscribe(fruit -> results.add(1));
        router.subscribe(fruit -> results.add(2));
        router.subscribe(fruit -> results.add(3));
        router.subscribe(fruit -> results.add(4));
        router.subscribe(veg -> results.add(5));
        router.subscribe(veg -> results.add(6));
        router.subscribe(veg -> results.add(7));
        router.subscribe(veg -> results.add(8));

        // This will hammer the router with events.
        // It returns after every event has been published (not delivered).
        runConcurrently(
            () -> router.publish(new Fruit("apple")),
            () -> router.publish(new Fruit("banana")));

        // Wait for all events to be delivered.
        EventUtils.sleep(500);
    }

    /**
     * Runs all the given runnables at precisely the same time.
     * Awaits until all threads are finished before returning.
     */
    public void runConcurrently(CheckedRunnable... runnables) {

        CountDownLatch readyLatch = new CountDownLatch(runnables.length);
        CountDownLatch doneLatch = new CountDownLatch(runnables.length);
        ExecutorService loadingPool = Executors.newCachedThreadPool();

        for (CheckedRunnable runnable : runnables) {
            loadingPool.submit(((CheckedRunnable)() -> {
                LOG.debug("Loader is waiting for the go-ahead...");
                readyLatch.countDown(); // Tell the world we're ready.
                readyLatch.await();     // Wait for everyone else to be ready.
                runnable.run();
                doneLatch.countDown();  // Tell the world we're done.
            }).unchecked());
        }

        // Wait for the loaders to synchronize, then run, then be done.
        try {
            doneLatch.await();
        } catch (Exception e) {
            EventUtils.throwUnchecked(e);
        }
    }

    public List<Integer> getResults() {
        return results;
    }

    @Test
    public void test() {

        List<Integer> results = getResults();
        // The results should be sporadic.
        out.println(results);
    }

    /**
     * Simple fruit record.
     */
    static final class Fruit {

        private final String name;

        Fruit(String name) {
            this.name = name;
        }
    }

    /**
     * Simple vegetable record.
     */
    static final class Vegetable {
        private final String name;

        Vegetable(String name) {
            this.name = name;
        }
    }
}
