package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * ThreadPoolsTest unit tests.
 */
public class ThreadPoolsTest {

    private static final Logger LOG = getLogger(ThreadPoolsTest.class);

    @Test
    @Disabled("threading integration test")
    public void test() {

        ThreadPoolExecutor pool = ThreadPools.newDynamicThreadPool();
        pool.execute(() -> threadLife(pool));
        EventUtils.sleep(500);
        pool.execute(() -> threadLife(pool));
        EventUtils.sleep(500);
        pool.execute(() -> threadLife(pool));
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        pool.execute(() -> threadLife(pool));
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        EventUtils.threadReport(pool);
    }

    private static void threadLife(ThreadPoolExecutor pool) {
        String threadName = Thread.currentThread().getName();
        LOG.info("Thread '{}' started, death in 5...", threadName);
        EventUtils.threadReport(pool);
        EventUtils.sleep(5000);
        LOG.info("Thread '{}' died.", threadName);
    }
}
