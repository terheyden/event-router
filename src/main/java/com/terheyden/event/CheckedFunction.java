package com.terheyden.event;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface CheckedFunction<T, R> extends Function<T, R> {

    /**
     * Static method to create a {@link CheckedFunction} from a lambda.
     */
    static <T, R> CheckedFunction<T, R> of(CheckedFunction<T, R> func) {
        return func;
    }

    /**
     * Apply the function to the given argument.
     * Equivalent to {@link Function#apply(Object)}, but allows throwing checked exceptions.
     */
    @SuppressWarnings("squid:S00112")
    R applyChecked(T item) throws Throwable;

    /**
     * Unchecked version of {@link Function#apply(Object)}.
     * Use this just as you would use {@link Function#apply(Object)}.
     * Any checked exceptions will be rethrown as unchecked automatically.
     *
     * @param item the function argument
     * @return the function result
     */
    @Override
    default R apply(T item) {
        try {
            return applyChecked(item);
        } catch (Throwable t) {
            return CheckedFunctionInternal.throwUnchecked(t);
        }
    }
}

/**
 * Defines a self-contained unchecked throw method.
 */
interface CheckedFunctionInternal {
    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R throwUnchecked(Throwable t) throws T {
        throw (T) t;
    }
}
