package com.terheyden.event;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadPoolExecutor;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract builder class for creating event routers.
 */
public final class EventRouters {

    private static final org.slf4j.Logger LOG = getLogger(EventRouters.class);

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
         * In some scenarios, like if there are many long-running subscribers,
         * it may be more performant to send events to subscribers asynchronously.
         */
        private boolean isMaxAsync = false;

        EventRouterBuilder() {
            // Package private.
        }

        /**
         * The max thread pool size used by this router's thread pool.
         * The default is {@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}.
         */
        public EventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        /**
         * By default, a {@link ThreadPools#newDynamicThreadPool(int)} is used. You can configure
         * the pool size by specifying {@link #maxThreadPoolSize(int)}. If you want to use your own
         * completely custom thread pool, you can specify it here. This is also a good setting to
         * use if you wish to share a single thread pool between many event routers.
         */
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
         * Setting this disables {@link #publishInOrder()}.
         */
        public EventRouterBuilder<T> maxAsync() {
            isMaxAsync = true;
            return this;
        }

        /**
         * If true, events will be delivered to subscribers
         * in the order that they subscribed to the event.
         * The default is true, since in most cases this is also the most performant setting.
         * Setting this disables {@link #maxAsync()}.
         */
        public EventRouterBuilder<T> publishInOrder() {
            isMaxAsync = false;
            return this;
        }

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event. The default is to log the exception at ERROR level.
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
         * In some scenarios, like if there are many long-running subscribers,
         * it may be more performant to send events to subscribers asynchronously.
         */
        private boolean isMaxAsync = false;

        private SubscriberExceptionHandler exceptionHandler = DEFAULT_EXCEPTION_HANDLER;

        EventQueryBuilder(int maxThreadPoolSize, @Nullable ThreadPoolExecutor customThreadPool) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            this.customThreadPool = customThreadPool;
        }

        /**
         * The max thread pool size used by this router's thread pool.
         * The default is {@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}.
         */
        public EventQueryBuilder<I, O> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        /**
         * By default, a {@link ThreadPools#newDynamicThreadPool(int)} is used. You can configure
         * the pool size by specifying {@link #maxThreadPoolSize(int)}. If you want to use your own
         * completely custom thread pool, you can specify it here. This is also a good setting to
         * use if you wish to share a single thread pool between many event routers.
         */
        public EventQueryBuilder<I, O> customThreadPool(ThreadPoolExecutor customThreadPool) {
            this.customThreadPool = customThreadPool;
            return this;
        }

        /**
         * This is an advanced setting: the default thread configuration is optimized for most use cases.
         * Use this setting if you expect to have many long-running subscribers and very few events.
         * Setting this disables {@link #publishInOrder()}.
         */
        public EventQueryBuilder<I, O> maxAsync() {
            isMaxAsync = true;
            return this;
        }

        /**
         * If true, events will be delivered to subscribers
         * in the order that they subscribed to the event.
         * The default is true, since in most cases this is also the most performant setting.
         * Setting this disables {@link #maxAsync()}.
         */
        public EventQueryBuilder<I, O> publishInOrder() {
            isMaxAsync = false;
            return this;
        }

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event. The default is to log the exception at ERROR level.
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

        /**
         * The max thread pool size used by this router's thread pool.
         * The default is {@link EventRouterGlobals#DEFAULT_THREADPOOL_SIZE}.
         */
        public ModifiableEventRouterBuilder<T> maxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        /**
         * By default, a {@link ThreadPools#newDynamicThreadPool(int)} is used. You can configure
         * the pool size by specifying {@link #maxThreadPoolSize(int)}. If you want to use your own
         * completely custom thread pool, you can specify it here. This is also a good setting to
         * use if you wish to share a single thread pool between many event routers.
         */
        public ModifiableEventRouterBuilder<T> customThreadPool(ThreadPoolExecutor customThreadPool) {
            this.customThreadPool = customThreadPool;
            return this;
        }

        /**
         * Set a custom exception handler for when an individual subscriber throws an exception
         * while handling an event. The default is to log the exception at ERROR level.
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
