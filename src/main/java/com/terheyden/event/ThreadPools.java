package com.terheyden.event;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPools class.
 */
public final class ThreadPools {

    /**
     * How long threads will remain idle before being terminated.
     */
    public static final int KEEP_ALIVE_SECS = 30;

    private ThreadPools() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Create a new dynamic thread pool that will grow and shrink as needed,
     * from 0 threads up to the specified max thread count.
     * When idle, the pool will shrink back down to 0 threads.
     */
    public static ThreadPoolExecutor newDynamicThreadPool(int maxThreadCount) {

        // BTW TPE prefers new threads until core count is reached.
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            maxThreadCount,               // We don't want core + extra threads, just always use core.
            maxThreadCount,               // Core count and max count are the same.
            KEEP_ALIVE_SECS,              // Idle threads will hang out this many secs.
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()); // Queue is unbounded so no messages are lost.

        // By default, the core threads will never get recycled, so we need to set this.
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    /**
     * Create a new dynamic thread pool that will grow and shrink as needed,
     * from 0 threads up to [CPU processors - 1] threads.
     * When idle, the pool will shrink back down to 0 threads.
     */
    public static ThreadPoolExecutor newDynamicThreadPool() {
        return newDynamicThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    }
}
