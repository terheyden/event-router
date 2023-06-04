package com.terheyden.event;

import java.util.ArrayList;
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
    private static final String WORLD = "world";

    private EventRouter<String> strRouter;

    @BeforeEach
    void beforeEach() {
        strRouter = EventRouters.createWithEventType(String.class).build();
    }

    @Test
    @DisplayName("Base case — sendEventToSubscribers one event, subscribe to it, and receive it")
    void testBaseCase() {

        List<String> results = publish(strRouter, HELLO);
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(HELLO);
    }

    @Test
    void testUnsubscribe() {

        AtomicInteger counter = new AtomicInteger(0);
        assertThat(strRouter.getSubscriptions()).isEmpty();

        UUID subscriptionId = strRouter.subscribe(e -> counter.incrementAndGet());

        strRouter.publish(HELLO);
        awaitEmpty(strRouter);
        assertThat(counter.get()).isEqualTo(1);

        counter.set(0);
        strRouter.unsubscribe(subscriptionId);
        assertThat(strRouter.getSubscriptions()).isEmpty();
        strRouter.publish(HELLO);
        awaitEmpty(strRouter);
        assertThat(counter.get()).isEqualTo(0);
    }

    @Test
    void testNoSubscribersEvent() {

            assertThatNoException().isThrownBy(() -> strRouter.publish(HELLO));
            awaitEmpty(strRouter);
    }

    @Test
    void testSubscriberException() {

        // Sandwich the exception between two other subscribers
        // so we can verify that the exception doesn't stop the event from being sent to the other subscribers.
        List<String> list1 = new ArrayList<>();
        strRouter.subscribe(list1::add);

        // Set up the next subscriber to throw.
        strRouter.subscribe(str -> {
            throw new RuntimeException("IGNORE: " + str);
        });

        List<String> list2 = new ArrayList<>();
        strRouter.subscribe(list2::add);

        publish(strRouter, HELLO, WORLD);

        assertThat(list1).contains(HELLO, WORLD);
        assertThat(list2).contains(HELLO, WORLD);
    }
}
