package com.terheyden.event;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EventRouterTest unit tests.
 */
public class EventRouterTest {

    private static final String HELLO = "hello";

    private EventRouter router;
    private ThreadPoolExecutor publishExecutor;

    @BeforeEach
    public void beforeEach() {
        EventRouterConfig config = new EventRouterConfig();
        router = new EventRouter(config);
        publishExecutor = config.receivedEventHandlerThreadPool();
    }

    @Test
    @DisplayName("Base case — sendEventToSubscribers one event, subscribe to it, and receive it")
    public void testBaseCase() {

        List<String> results = EventTester.publish(router, String.class, HELLO);
        assertEquals(1, results.size());
        assertEquals(HELLO, results.get(0));
    }

    @Test
    public void testUnsubscribe() {

        AtomicInteger counter = new AtomicInteger(0);

        UUID subscriptionId = router.subscribe(
            String.class,
            e -> counter.incrementAndGet());

        router.publish(HELLO);
        EventTester.awaitEmpty(publishExecutor);

        assertTrue(router.unsubscribe(subscriptionId));
        assertFalse(router.unsubscribe(subscriptionId));

        router.publish(HELLO);
        EventTester.awaitEmpty(publishExecutor);

        // Should only have been called once.
        assertEquals(1, counter.get());
    }

    @Test
    public void testEventInterrupts() {

        // We'll fire an int event, then a string event will happen in the middle of it.
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<String> stringEvents = new AtomicReference<>("");

        // Int event 1:
        router.subscribe(
            Integer.class,
            intVal -> {
                // This should be called first since it's in-order.
                assertTrue(counter.compareAndSet(0, intVal));
                // This sendEventToSubscribers() happens while one is already in progress.
                // If the interrupt were allowed to happen, the string event would fire
                // right now before the second int event.
                router.publish(String.valueOf(intVal));
            });

        // Int event 2:
        router.subscribe(
            Integer.class,
            intVal -> {
                // The in-process sendEventToSubscribers should call this before the String sendEventToSubscribers,
                // therefore the string ref should still be empty:
                assertEquals("", stringEvents.get());
                assertTrue(counter.compareAndSet(intVal, intVal + 1));
            });

        // On String event, set our stringEvents ref.
        router.subscribe(
            String.class,
            str -> {
                // Update our string ref.
                stringEvents.compareAndSet("", str);
                // Verify the second int event happened first.
                int intVal = Integer.parseInt(str);
                assertEquals(intVal + 1, counter.get());
            });

        // Publish an Integer event to run all our tests.
        router.publish(1);
    }

    @Test
    void testUnknownEventTypeReceived() {
        router.publish(1L);
    }
}
