package com.terheyden.event;

import java.util.ArrayList;
import java.util.List;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;

public class EventChain<T> {

    private final List<CheckedFunction1<T, T>> mutableHandlers = new ArrayList<>();

    public void addHandler(CheckedFunction1<T, T> handler) {
        mutableHandlers.add(handler);
    }

    public void removeHandler(CheckedFunction1<T, T> handler) {
        mutableHandlers.remove(handler);
    }

    public void clearHandlers() {
        mutableHandlers.clear();
    }

    public Try<T> send(T input) {

        Try<T> result = Try.success(input);

        for (CheckedFunction1<T, T> handler : mutableHandlers) {
            result = result.mapTry(handler);
        }

        return result;
    }
}
