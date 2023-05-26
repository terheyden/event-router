package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract builder class for creating event routers.
 */
public final class EventRouters {

    private EventRouters() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Create a new event router builder that sends events of the given class type.
     * @param eventType type of event objects to send
     */
    public static <T> EventRouterBuilder<T> createWithEventType(Class<T> eventType) {
        return new EventRouterBuilder<>();
    }

    static ThreadPoolExecutor createThreadPool(int maxThreadCount) {
        return ThreadPools.newDynamicThreadPool(maxThreadCount);
    }

    public static class EventRouterBuilder<T> {

        private int maxThreadPoolSize = EventRouterGlobals.DEFAULT_THREADPOOL_SIZE;

        @Nullable private ThreadPoolExecutor customThreadPool = null;

        /**
         * The default for a standard event router is {@link ThreadPoolSendStrategy}, since
         * we assume that most services are network-bound and not CPU-bound. For CPU-bound
         * services, we recommend {@link SequentialSendStrategy}.
         * This var will determine the approach we build with.
         */
        private boolean isMaxAsync = false;

        EventRouterBuilder() {
            // Package private.
        }

        public EventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public EventRouterBuilder<T> customThreadPool(ThreadPoolExecutor customThreadPool) {
            this.customThreadPool = customThreadPool;
            return this;
        }

        /**
         * Indicates that sent events are expected to return a reply of the given class type.
         * For example, let's make a router that accepts a String event and replies with the String's length:
         * <pre>
         * {@code
         * EventRouters
         *     .createWithEventType(MyEvent.class)
         *     .eventReplyType(Integer.class)
         *     .build();
         * }
         * </pre>
         *
         * @param replyType the type of reply expected from the event
         */
        public <O> EventQueryBuilder<T, O> eventReplyType(Class<O> replyType) {
            return new EventQueryBuilder<>(maxThreadPoolSize, customThreadPool);
        }

        public ModifiableEventRouterBuilder<T> modifiableEvents() {
            return new ModifiableEventRouterBuilder<>(maxThreadPoolSize, customThreadPool);
        }

        /**
         * This is an advanced setting: the default thread configuration is optimized for most use cases.
         * Use this setting if you expect to have many long-running subscribers and very few events.
         */
        public EventRouterBuilder<T> maxAsync() {
            isMaxAsync = true;
            return this;
        }

        public EventRouter<T> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            SendEventStrategy<T> sendStrategy = isMaxAsync
                ? new ThreadPoolSendStrategy<>(threadPool)
                : new SequentialSendStrategy<>();

            return new EventRouterImpl<>(threadPool, sendStrategy);
        }
    }

    /**
     * Builder for event routers with events that return a reply.
     * Created by specifying {@link EventRouterBuilder#eventReplyType(Class)}.
     */
    public static class EventQueryBuilder<I, O> {

        private int maxThreadPoolSize;

        @Nullable private ThreadPoolExecutor customThreadPool;

        /**
         * The default for a standard event router is {@link ThreadPoolSendStrategy}, since
         * we assume that most services are network-bound and not CPU-bound. For CPU-bound
         * services, we recommend {@link SequentialSendStrategy}.
         * This var will determine the approach we build with.
         */
        private boolean isMaxAsync = false;

        EventQueryBuilder(int maxThreadPoolSize, @Nullable ThreadPoolExecutor customThreadPool) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            this.customThreadPool = customThreadPool;
        }

        public EventQueryBuilder<I, O> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public EventQueryBuilder<I, O> customThreadPool(ThreadPoolExecutor customThreadPool) {
            this.customThreadPool = customThreadPool;
            return this;
        }

        /**
         * This is an advanced setting: the default thread configuration is optimized for most use cases.
         * Use this setting if you expect to have many long-running subscribers and very few events.
         */
        public EventQueryBuilder<I, O> maxAsync() {
            isMaxAsync = true;
            return this;
        }

        public EventQuery<I, O> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            SendEventStrategy<I> sendStrategy = isMaxAsync
                ? new EventQuerySendAsyncStrategy<>(threadPool)
                : new EventQuerySendSequentialStrategy<>();

            return new EventQueryImpl<>(threadPool, sendStrategy);
        }
    }

    /**
     * Build a new event router that supports events that may be modified during delivery.
     * Only sequential delivery is supported in order to make that possible.
     */
    public static class ModifiableEventRouterBuilder<T> {

        private int maxThreadPoolSize;

        @Nullable private ThreadPoolExecutor customThreadPool;

        ModifiableEventRouterBuilder(int maxThreadPoolSize, @Nullable ThreadPoolExecutor customThreadPool) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            this.customThreadPool = customThreadPool;
        }

        public ModifiableEventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public ModifiableEventRouterBuilder<T> customThreadPool(ThreadPoolExecutor customThreadPool) {
            this.customThreadPool = customThreadPool;
            return this;
        }

        public ModifiableEventRouter<T> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            return new ModifiableEventRouterImpl<>(threadPool);
        }
    }
}
