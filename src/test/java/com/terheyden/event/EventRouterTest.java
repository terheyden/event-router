package com.terheyden.event;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.terheyden.event.EventTester.awaitEmpty;
import static com.terheyden.event.EventTester.publish;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * EventRouterTest unit tests.
 */
class EventRouterTest {

    private static final String HELLO = "hello";

    private EventRouterImpl<String> router;

    @BeforeEach
    void beforeEach() {
        router = new EventRouterImpl<>();
    }

    @Test
    @DisplayName("Base case — sendEventToSubscribers one event, subscribe to it, and receive it")
    void testBaseCase() {

        List<String> results = publish(router, HELLO);
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(HELLO);
    }

    @Test
    void testUnsubscribe() {

        AtomicInteger counter = new AtomicInteger(0);
        assertThat(router.getSubscribers()).isEmpty();

        UUID subscriptionId = router.subscribe(e -> counter.incrementAndGet());

        router.publish(HELLO);
        awaitEmpty(router);

        assertThat(counter.get()).isEqualTo(1);
        router.unsubscribe(subscriptionId);
        assertThat(router.getSubscribers()).isEmpty();
    }

    @Test
    void testNoSubscribersEvent() {

            assertThatNoException().isThrownBy(() -> router.publish(HELLO));
            awaitEmpty(router);
    }

    @Test
    void testSubscriberException() {
        // Special event if subscriber throws.
        // Should not interrupt other subscribers.
    }
}
