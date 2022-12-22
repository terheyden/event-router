package com.terheyden.event;

import java.util.function.Function;

/**
 * Wrap a {@link Runnable} into a {@link Function}
 * without creating a second lambda (closure) object.
 */
public class RunnableFunction<T, R> implements Function<T, R> {

    private final Runnable runnable;

    public RunnableFunction(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public R apply(T elem) {
        runnable.run();
        return null;
    }
}
