package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract builder class for creating event routers.
 */
public final class EventRouters {

    private static final Logger LOG = getLogger(EventRouters.class);

    // Package-private so tests can use it also, maybe also constructors can default to it.
    static final SubscriberExceptionHandler DEFAULT_EXCEPTION_HANDLER = (err, eventObj) ->
        LOG.error("Subscriber threw exception while handling event: {}", eventObj, err);

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
        private SubscriberExceptionHandler exceptionHandler = DEFAULT_EXCEPTION_HANDLER;

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

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event.
         */
        public EventRouterBuilder<T> exceptionHandler(SubscriberExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public EventRouter<T> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            SendEventStrategy<T> sendStrategy = isMaxAsync
                ? new ThreadPoolSendStrategy<>(exceptionHandler, threadPool)
                : new SequentialSendStrategy<>(exceptionHandler);

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

        private SubscriberExceptionHandler exceptionHandler = DEFAULT_EXCEPTION_HANDLER;

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

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event.
         */
        public EventQueryBuilder<I, O> exceptionHandler(SubscriberExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public EventQuery<I, O> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            SendEventStrategy<I> sendStrategy = isMaxAsync
                ? new EventQuerySendAsyncStrategy<>(exceptionHandler, threadPool)
                : new EventQuerySendSequentialStrategy<>(exceptionHandler);

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
        private SubscriberExceptionHandler exceptionHandler = DEFAULT_EXCEPTION_HANDLER;

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

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event.
         */
        public ModifiableEventRouterBuilder<T> exceptionHandler(SubscriberExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public ModifiableEventRouter<T> build() {

            ThreadPoolExecutor threadPool = customThreadPool == null
                ? createThreadPool(maxThreadPoolSize)
                : customThreadPool;

            return new ModifiableEventRouterImpl<>(exceptionHandler, threadPool);
        }
    }
}
