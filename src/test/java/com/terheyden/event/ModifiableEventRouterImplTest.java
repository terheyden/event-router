package com.terheyden.event;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * ModifiableEventRouterImplTest unit tests.
 */
class ModifiableEventRouterImplTest {

    private static final Logger LOG = getLogger(ModifiableEventRouterImplTest.class);

    private final String testStr = "Hello World!";

    @Test
    void test() {

        // Create a new event router.
        ModifiableEventRouterImpl<String> router = new ModifiableEventRouterImpl<>();

        // Add some subscribers in order.
        // First verify the start string.
        router.subscribe(str -> {
            assertThat(str).isEqualTo(testStr);
            LOG.debug(str);
            return str;
        });

        router.subscribe(String::toUpperCase);

        router.subscribe(str -> {
            assertThat(str).isEqualTo(testStr.toUpperCase());
            LOG.debug(str);
            return str;
        });

        router.subscribe(String::toLowerCase);

        router.subscribe(str -> {
            assertThat(str).isEqualTo(testStr.toLowerCase());
            LOG.debug(str);
            return str;
        });

        // And now we test the dropping of the event.
        router.subscribe(str -> null);

        // Should never get here.
        router.subscribe(str -> Assertions.fail("Umm I should be dead."));

        // Okay! Our router is all set up, now we test.
        LOG.debug("Starting test (this should be on a different thread).");
        router.publish(testStr);
        EventUtils.sleep(300);
    }
}
