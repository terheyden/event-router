package com.terheyden.event;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Iterator;

/**
 * Returns a global static empty {@link AbstractQueue} instance.
 */
public final class EmptyQueue<T> extends AbstractQueue<T> {

    /**
     * A thread-safe empty immutable queue.
     */
    public static final EmptyQueue<?> INSTANCE = new EmptyQueue<>();

    private EmptyQueue() {
        // Don't allow instantiation.
    }

    /**
     * A thread-safe empty immutable queue.
     * Always returns the same global instance, so
     * {@code EmptyQueue.instance() == EmptyQueue.instance()}.
     */
    @SuppressWarnings("unchecked")
    public static <T> EmptyQueue<T> instance() {
        return (EmptyQueue<T>) INSTANCE;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean offer(T t) {
        throw new UnsupportedOperationException("Empty immutable queue.");
    }

    // poll() returns null in its contract.
    @Override
    public T poll() {
        return null;
    }

    // peek() returns null in its contract.
    @Override
    public T peek() {
        return null;
    }
}
