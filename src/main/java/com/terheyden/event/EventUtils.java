package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

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

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throwUnchecked(e);
        }
    }

    /**
     * Extract useful stats from a thread pool.
     */
    public static String threadReport(ThreadPoolExecutor pool) {

        StringBuilder bui = new StringBuilder();

        bui.append("Pool size: ").append(pool.getPoolSize()).append('\n');
        bui.append("Active count: ").append(pool.getActiveCount()).append('\n');
        bui.append("Queue size: ").append(pool.getQueue().size()).append('\n');
        bui.append("Task count: ").append(pool.getTaskCount()).append('\n');
        bui.append("Completed task count: ").append(pool.getCompletedTaskCount()).append('\n');

        return bui.toString();
    }
}
