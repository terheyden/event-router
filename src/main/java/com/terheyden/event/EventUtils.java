package com.terheyden.event;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

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

    @SuppressWarnings("unchecked")
    static <T> ConsumerFunction1<Class<?>, Object> consumerToFunction(CheckedConsumer<T> eventHandler) {
        return new ConsumerFunction1<Class<?>, Object>((CheckedConsumer) eventHandler);
    }

    @SuppressWarnings("unchecked")
    static <T, R> CheckedFunction1<Object, Object> functionToFunction(CheckedFunction1<T, R> eventHandler) {
        return (CheckedFunction1<Object, Object>) eventHandler;
    }
}
