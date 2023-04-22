package com.terheyden.event;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.terheyden.event.EventTester.awaitEmpty;
import static com.terheyden.event.EventTester.publish;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * EventRouterTest unit tests.
 */
class EventRouterTest {

    private static final String HELLO = "hello";

    private EventRouter router;

    @BeforeEach
    void beforeEach() {
        router = new EventRouter();
    }

    @Test
    @DisplayName("Base case — sendEventToSubscribers one event, subscribe to it, and receive it")
    void testBaseCase() {

        List<String> results = publish(router, String.class, HELLO);
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(HELLO);
    }

    @Test
    void testUnsubscribe() {

        AtomicInteger counter = new AtomicInteger(0);

        UUID subscriptionId = router.subscribe(
            String.class,
            e -> counter.incrementAndGet());

        router.publish(HELLO);
        awaitEmpty(router);

        assertThat(router.unsubscribe(subscriptionId)).isTrue();
        assertThat(router.unsubscribe(subscriptionId)).isFalse();

        router.publish(HELLO);
        awaitEmpty(router);

        // Should only have been called once.
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void testEventInterrupts() {

        // We'll fire an int event, then a string event will happen in the middle of it.
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<String> stringEvents = new AtomicReference<>("");

        // Int event 1:
        router.subscribe(
            Integer.class,
            intVal -> {
                // This should be called first since it's in-order.
                assertThat(counter.compareAndSet(0, intVal)).isTrue();
                // This sendEventToSubscribers() happens while one is already in progress.
                // If the interrupt were allowed to happen, the string event would fire
                // right now before the second int event.
                router.publish(valueOf(intVal));
            });

        // Int event 2:
        router.subscribe(
            Integer.class,
            intVal -> {
                // The in-process sendEventToSubscribers should call this before the String sendEventToSubscribers,
                // therefore the string ref should still be empty:
                assertThat(stringEvents.get()).isEmpty();
                assertThat(counter.compareAndSet(intVal, intVal + 1)).isTrue();
            });

        // On String event, set our stringEvents ref.
        router.subscribe(
            String.class,
            str -> {
                // Update our string ref.
                stringEvents.compareAndSet("", str);
                // Verify the second int event happened first.
                int intVal = parseInt(str);
                assertThat(counter.get()).isEqualTo(intVal + 1);
            });

        // Publish an Integer event to run all our tests.
        router.publish(1);
    }

    @Test
    void testUnknownEventTypeReceived() {
        assertThatNoException().isThrownBy(() -> router.publish(1L));
    }

    @Test
    void testNoSubscribersEvent() {
        // Special event if no subscribers.
    }

    @Test
    void testSubscriberException() {
        // Special event if subscriber throws.
        // Should not interrupt other subscribers.
    }

    @Test
    void testPublishSubclassEventObject() {
        // Should be able to publish a subclass of the event type.
    }
}
