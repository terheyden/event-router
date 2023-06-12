package com.terheyden.event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.terheyden.event.EventUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * ThreadTest class.
 */
public class ThreadTest {

    @Test
    @DisplayName("When a single thread dies, there is no recovery")
    public void testSingleThreadDeath() {

        Thread singleThread = new Thread(() -> {
            sleep(100);
            throw new IllegalStateException("Time for this single thread to die.");
        });

        singleThread.start();
        sleep(200);
        assertThat(singleThread.isAlive()).isFalse();
    }

    @Test
    @DisplayName("When a pool thread dies, it is replaced")
    public void testPoolDeath() {

        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(10);

        IntStream.range(0, 10).forEach(num -> pool.execute(() -> {
            latch.countDown();
            throw new IllegalStateException("IGNORE: counting down: " + num);
        }));

        assertThatNoException().isThrownBy(latch::await);
    }
}
