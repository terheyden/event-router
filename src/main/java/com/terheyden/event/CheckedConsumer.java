package com.terheyden.event;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Consumer<T> {

    /**
     * Static method to create a {@link CheckedConsumer} from a lambda.
     */
    static <T> CheckedConsumer<T> of(CheckedConsumer<T> consumer) {
        return consumer;
    }

    /**
     * Accept the given item. Equivalent to {@link Consumer#accept(Object)},
     * but allows throwing checked exceptions.
     */
    void acceptChecked(T item) throws Throwable;

    /**
     * Unchecked version of {@link Consumer#accept(Object)}.
     * Use this just as you would use {@link Consumer#accept(Object)}.
     * Any checked exceptions will be rethrown as unchecked automatically.
     *
     * @param item the input argument
     */
    @Override
    default void accept(T item) {
        try {
            acceptChecked(item);
        } catch (Throwable t) {
            CheckedConsumerInternal.throwUnchecked(t);
        }
    }
}

/**
 * Defines a self-contained unchecked throw method.
 */
interface CheckedConsumerInternal {
    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R throwUnchecked(Throwable t) throws T {
        throw (T) t;
    }
}
