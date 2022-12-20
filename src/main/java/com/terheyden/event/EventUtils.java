package com.terheyden.event;

/**
 * EventUtils class.
 */
/* package */ final class EventUtils {

    private EventUtils() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Throw any exception unchecked.
     */
    @SuppressWarnings("unchecked")
    /* package */ static <E extends Throwable, R> R throwUnchecked(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
