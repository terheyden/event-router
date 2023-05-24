package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract builder class for creating event routers.
 */
public final class EventRouters {

    private EventRouters() {
        // Private since this class shouldn't be instantiated.
    }

    public static <T> EventRouterBuilder<T> createWithEventType(Class<T> eventType) {
        return new EventRouterBuilder<>();
    }

    static ThreadPoolExecutor createThreadPool(int maxThreadCount) {
        return ThreadPools.newDynamicThreadPool(maxThreadCount);
    }

    public static class EventRouterBuilder<T> {

        private int maxThreadPoolSize = EventRouterGlobals.DEFAULT_THREADPOOL_SIZE;

        EventRouterBuilder() {
            // Package private.
        }

        public EventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public <O> EventQueryBuilder<T, O> eventReplyType(Class<O> replyType) {
            return new EventQueryBuilder<>(maxThreadPoolSize);
        }

        public ModifiableEventRouterBuilder<T> modifiableEvents() {
            return new ModifiableEventRouterBuilder<>(maxThreadPoolSize);
        }

        public EventRouter<T> build() {
            return new EventRouterImpl<>(createThreadPool(maxThreadPoolSize));
        }
    }

    public static class EventQueryBuilder<I, O> {

        private int maxThreadPoolSize;

        EventQueryBuilder(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
        }

        public EventQueryBuilder<I, O> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public EventQuery<I, O> build() {
            return new EventQueryImpl<>(createThreadPool(maxThreadPoolSize));
        }
    }

    public static class ModifiableEventRouterBuilder<T> {

        private int maxThreadPoolSize;

        ModifiableEventRouterBuilder(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
        }

        public ModifiableEventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public ModifiableEventRouter<T> build() {
            return new ModifiableEventRouterImpl<>(createThreadPool(maxThreadPoolSize));
        }
    }
}
