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

        /**
         * The default for a standard event router is {@link ThreadPoolSendStrategy}, since
         * we assume that most services are network-bound and not CPU-bound. For CPU-bound
         * services, we recommend {@link SequentialSendStrategy}.
         * This var will determine the approach we build with.
         */
        private boolean networkOptimized = true;

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

        public EventRouterBuilder<T> networkOptimized() {
            networkOptimized = true;
            return this;
        }

        public EventRouterBuilder<T> cpuOptimized() {
            networkOptimized = false;
            return this;
        }

        public EventRouter<T> build() {

            ThreadPoolExecutor threadPool = createThreadPool(maxThreadPoolSize);

            SendEventToSubscriberStrategy<T> sendStrategy = networkOptimized
                ? new ThreadPoolSendStrategy<>(threadPool)
                : new SequentialSendStrategy<>();

            return new EventRouterImpl<>(threadPool, sendStrategy);
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
