package com.terheyden.event;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EventRouterTest unit tests.
 */
public class EventRouterTest {

    private static final String HELLO = "hello";
    public static final String IGNORE = "IGNORE";

    private EventRouter router;

    @BeforeEach
    public void beforeEach() {
        router = new EventRouter();
    }

    @Test
    @DisplayName("Base case — publish one event, subscribe to it, and receive it")
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
        router.unsubscribe(String.class, subscriptionId);
        router.publish(HELLO);

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
                // This publish() happens while one is already in progress.
                // If the interrupt were allowed to happen, the string event would fire
                // right now before the second int event.
                router.publish(String.valueOf(intVal));
            });

        // Int event 2:
        router.subscribe(
            Integer.class,
            intVal -> {
                // The in-process publish should call this before the String publish,
                // therefore the string ref should still be empty:
                assertEquals("", stringEvents.get());
                assertTrue(counter.compareAndSet(intVal, intVal + 1));
            });

        // On String event, set our stringEvents ref.
        router.subscribe(
            String.class,
            str -> {
                // Update our string ref.
                stringEvents.set(str);
                // Verify the second int event happened first.
                int intVal = Integer.parseInt(str);
                assertEquals(intVal + 1, counter.get());
            });

        // Publish an Integer event to run all our tests.
        router.publish(1);
    }

    @Test
    public void testExceptionHandling() {

        // Exceptions are sent as events.
        AtomicReference<Exception> exceptionRef = new AtomicReference<>(new RuntimeException(IGNORE));
        router.subscribe(EventRouterPublishException.class, exceptionRef::set);

        // Throw an exception, so we can test.
        router.subscribe(
            String.class,
            str -> {
                throw new RuntimeException(IGNORE);
            });

        router.publish(HELLO);
        // Should no longer be a RuntimeException.
        assertTrue(exceptionRef.get() instanceof EventRouterPublishException);
    }

    @Test
    public void testNestedExceptions() {

        // Throw an exception when a string is sent.
        router.subscribe(
            String.class,
            str -> {
                throw new IllegalArgumentException(IGNORE);
            });

        // Throw again when an exception is handled.
        AtomicReference<Exception> exceptionRef = new AtomicReference<>(new RuntimeException(IGNORE));
        router.subscribe(
            EventRouterPublishException.class,
            ex -> {
                // Change the default RuntimeException into the IllegalArgumentException.
                exceptionRef.set(ex);
                // We're going to throw while handling an exception.
                throw new IllegalStateException(IGNORE);
            });

        // Start the test:
        router.publish(HELLO);

        // 1. The AtomicRef starts as a RuntimeException
        // 2. The first exception is thrown, and the AtomicRef is set to an IllegalArgumentException
        // 3. When the second exception is thrown, it should just be logged and not published
        Throwable cause = exceptionRef.get().getCause();
        assertTrue(cause instanceof IllegalArgumentException);
    }
}
