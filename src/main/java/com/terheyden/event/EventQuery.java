package com.terheyden.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import io.vavr.CheckedFunction1;

/**
 * EventQuery class.
 */
public class EventQuery<I, O> {

    private final List<CheckedFunction1<I, O>> mutableHandlers = new ArrayList<>();

    public void addHandler(CheckedFunction1<I, O> handler) {
        mutableHandlers.add(handler);
    }

    public void removeHandler(CheckedFunction1<I, O> handler) {
        mutableHandlers.remove(handler);
    }

    public void clearHandlers() {
        mutableHandlers.clear();
    }

    public O query(I input) {
        // TODO Optional?
        return queryAll(input).findAny();
    }

    public CompletableFuture<O> queryAsync(I input) {
        // TODO use threadpool here.
        return CompletableFuture.supplyAsync(() -> query(input));
    }

    public Stream<O> queryAll(I input) {
        // TODO try or throw or what?
        return mutableHandlers
            .stream()
            .map(handler -> handler.unchecked().apply(input));
    }
}
