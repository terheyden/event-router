package com.terheyden.event;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPools class.
 */
public final class ThreadPools {

    private ThreadPools() {
        // Private since this class shouldn't be instantiated.
    }

    public static ThreadPoolExecutor newDynamicThreadPool(int maxThreadCount) {

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
            10,                           // Keep-alive time; 10 secs is an eternity re: thread creation.
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()); // Queue is unbounded.

        // By default, the core threads will never get recycled, so we need to set this.
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    public static ThreadPoolExecutor newDynamicThreadPool() {
        return newDynamicThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
    }
}
