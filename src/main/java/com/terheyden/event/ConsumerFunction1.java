package com.terheyden.event;

import java.util.function.Consumer;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * Wrap a {@link Consumer} into a {@link CheckedFunction1}
 * without creating a second lambda (closure) object.
 */
public class ConsumerFunction1<T, R> implements CheckedFunction1<T, R> {

    private static final long serialVersionUID = 1L;

    private final CheckedConsumer<T> consumer;

    public ConsumerFunction1(CheckedConsumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public R apply(T elem) throws Throwable {
        consumer.accept(elem);
        return null;
    }
}
