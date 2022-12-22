package com.terheyden.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An {@link EventPublisher} that uses a thread pool to publish events.
 * This layer's whole job is just to configure the threadpool for subclasses.
 * This layer simply provides: {@link #execute(Runnable)}
 */
public abstract class BaseThreadPoolPublisher implements EventPublisher {

    private final ExecutorService threadPool;

    /**
     * Create a new {@link BaseThreadPoolPublisher} that uses an unbounded queue and up to [maxThreadCount]
     * threads to deliver events. Idle threads will be terminated until needed again.
     *
     * @param maxThreadCount The maximum number of threads to use.
     */
    protected BaseThreadPoolPublisher(int maxThreadCount) {

        // TPE has very complex behavior:
        //   As events come in, core threads are created and used.
        //   Once the queue is full, non-core threads are created and used.
        //   Once the queue is empty, non-core threads are destroyed.
        //   Core threads never die.
        //   https://stackoverflow.com/questions/17659510/core-pool-size-vs-maximum-pool-size-in-threadpoolexecutor
        //
        // How we are using it:
        //   Our queue is unbounded, so we want all threads to be core threads.
        //   Additionally, we want core threads to be recycled when not in use.
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            maxThreadCount,               // Core pool size is essentially our max pool size.
            maxThreadCount,               // Max pool size is irrelevant since we use a LinkedBlockingQueue.
            10,                           // 10 secs is an eternity re: thread creation.
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()); // Queue is unbounded.

        // By default, the core threads will never get recycled, so we need to set this.
        pool.allowCoreThreadTimeOut(true);
        this.threadPool = pool;
    }

    /**
     * Create a new {@link BaseThreadPoolPublisher} that uses the given thread pool to publish events.
     */
    protected BaseThreadPoolPublisher(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Add a task to our threadpool.
     */
    protected void execute(Runnable task) {
        threadPool.execute(task);
    }

    protected CompletableFuture<Object> executeAndReturn(Runnable task) {
        return CompletableFuture.supplyAsync(() -> {
            task.run();
            return null;
        }, threadPool);
    }
}
