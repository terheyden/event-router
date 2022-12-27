package com.terheyden.event;

import io.vavr.control.Try;

/**
 * TestUtils class.
 */
public final class TestUtils {

    private TestUtils() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Helper method to sleep peacefully.
     */
    public static void sleep(int millis) {
        try {
            Try.run(() -> Thread.sleep(millis));
        } catch (Exception e) {
            EventUtils.throwUnchecked(e);
        }
    }
}
