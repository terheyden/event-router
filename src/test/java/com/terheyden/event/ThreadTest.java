package com.terheyden.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static com.terheyden.event.TestUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * ThreadTest class.
 */
public class ThreadTest {

    @Test
    public void testSingleThreadDeath() {

        Thread singleThread = new Thread(() -> {
            sleep(100);
            throw new IllegalStateException("Time for this single thread to die.");
        });

        singleThread.start();
        sleep(200);
        assertFalse(singleThread.isAlive());
    }

    @Test
    public void testPoolDeath() {

        ExecutorService pool = Executors.newSingleThreadExecutor();

        IntStream.range(0, 10).forEach(num -> pool.execute(() -> {
            sleep(100);
            throw new IllegalStateException("Time for this pool thread to die.");
        }));

        sleep(500);
    }
}
