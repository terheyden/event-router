package com.terheyden.event;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.vavr.CheckedConsumer;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventRouterLoadTest class.
 */
class EventRouterLoadTest {

    static final Logger LOG = getLogger(EventRouterLoadTest.class);

    /**
     * Test throughput of the default config, assuming it's a CPU-intensive system.
     * 855k / sec.
     */
    @Test
    @Disabled("load test")
    void testDefaultCPUConfig() throws InterruptedException {
        int simulatedWorkDelayMs = 0; // We want to test throughput, so no simulated delay.
        int numberOfEvents = 80_000;
        runLoadTest(EventRouters
            .createWithEventType(String.class)
            .build(),
            numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test throughput of the default config, but they've specified the cpu-optimized flag.
     * 2.5M / sec.
     */
    @Test
    @Disabled("load test")
    void testOptimizedCPUConfig() throws InterruptedException {
        int simulatedWorkDelayMs = 0; // We want to test throughput, so no simulated delay.
        int numberOfEvents = 200_000;
        runLoadTest(EventRouters
            .createWithEventType(String.class)
            .cpuOptimized()
            .build(),
            numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test throughput of the default config, assuming it's a CPU-intensive system.
     * 2M / sec.
     */
    @Test
    @Disabled("load test")
    void testDefaultModifiableRouterForCPU() throws InterruptedException {

        int simulatedWorkDelayMs = 0; // We want to test throughput, so no simulated delay.
        int numberOfEvents = 50_000;

        ModifiableEventRouter<String> modifiableRouter = EventRouters
            .createWithEventType(String.class)
            .modifiableEvents()
            .build();

        // Adapt it.
        ModifiableEventRouterAdapter<String> adapter = new ModifiableEventRouterAdapter<>(modifiableRouter);

        runLoadTest(adapter, numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test throughput of the default config, assuming it's an IO-bound system.
     * The default should be multi-threaded (which is ideal for IO).
     */
    @Test
    @Disabled("load test")
    void testDefaultIOConfig() throws InterruptedException {
        int simulatedWorkDelayMs = 3; // Let's say each task takes 3ms to complete.
        int numberOfEvents = 50_00;
        runLoadTest(EventRouters
            .createWithEventType(String.class)
            .build(),
            numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test throughput of the default config, assuming it's an IO-bound system.
     */
    @Test
    @Disabled("load test")
    void testSyncIOConfig() throws InterruptedException {
        int simulatedWorkDelayMs = 3; // Let's say each task takes 3ms to complete.
        int numberOfEvents = 10_000;
        runLoadTest(EventRouters
            .createWithEventType(String.class)
            .cpuOptimized() // Whoops don't do this. Only doing this to test performance difference.
            .build(),
            numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test optimized settings for an IO-bound system.
     */
    @Test
    @Disabled("load test")
    void testRecommendedIOConfig() throws InterruptedException {

        int simulatedWorkDelayMs = 3; // Let's say each task takes 3ms to complete.
        int threadPoolSize = 1000;    // For large IO-bound systems we recommend around 1000 threads.
        int numberOfEvents = 10_000;

        EventRouter<String> eventRouter = EventRouters.createWithEventType(String.class)
            .maxThreadPoolSize(threadPoolSize)
            .build();
        runLoadTest(eventRouter, numberOfEvents, simulatedWorkDelayMs);
    }

    private void runLoadTest(
        EventRouter<String> eventRouter,
        int eventCount,
        int eventDelayMs)
        throws InterruptedException {

        // Let's see how fast we can chew through these events.
        // The number delivered is: eventCount * subscriberCount. 950k / sec.
        //
        // Most events would have maybe a handful of subscribers... we'll use 100.
        int subscriberCount = 100;
        // We'll use a CountDownLatch to wait for all subscribers to finish.
        CountDownLatch latch = new CountDownLatch(subscriberCount);

        LOG.info("Adding {} subscribers...", subscriberCount);
        List<LoadSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < subscriberCount; i++) {
            subscribers.add(new LoadSubscriber(eventRouter, latch, eventCount, eventDelayMs));
        }

        LOG.info("Publishing {} events...", eventCount);
        // Make our event payload objects.
        String stringEvent = "hi";
        long start = System.currentTimeMillis();

        for (int i = 0; i < eventCount; i++) {
            eventRouter.publish(stringEvent);
        }

        LOG.info("All events queued; waiting for subscribers to finish...");
        //reportAndAwaitLatch(eventRouter, latch);
        latch.await();

        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        long totalDelivered = eventCount * subscriberCount;
        DecimalFormat df = new DecimalFormat("#.00");
        LOG.info("Done in {} secs ({} msg/sec)", seconds, df.format(totalDelivered / seconds));
        LOG.debug(EventUtils.threadReport(eventRouter.getThreadPool()));
        reportIncompleteListeners(subscribers, eventCount);
    }

    private static void reportAndAwaitLatch(EventRouter<String> eventRouter, CountDownLatch latch) {

        ThreadPoolExecutor pool = eventRouter.getThreadPool();
        BlockingQueue<Runnable> poolQueue = pool.getQueue();

        while (latch.getCount() > 0) {

            long taskCount = pool.getTaskCount();
            long completedTaskCount = pool.getCompletedTaskCount();
            int activeCount = pool.getActiveCount();
            int poolSize = pool.getPoolSize();
            int largestPoolSize = pool.getLargestPoolSize();
            int maximumPoolSize = pool.getMaximumPoolSize();
            int corePoolSize = pool.getCorePoolSize();
            int poolQueueSize = poolQueue.size();

            StringBuilder builder = new StringBuilder();
            builder.append(format("Tasks: %d running, %d completed, %d total. ", activeCount, completedTaskCount, taskCount));
            builder.append(format("Pool: %d current, %d largest, %d max, %d core. Queue: %d", poolSize, largestPoolSize, maximumPoolSize, corePoolSize, poolQueueSize));

            // Report the threadpool load.
            LOG.info(builder.toString());
            EventUtils.sleep(250);
        }
    }

    private static void reportIncompleteListeners(List<LoadSubscriber> listeners, int eventCount) {
        for (LoadSubscriber listener : listeners) {
            long listenerCount = listener.getCounter();
            if (listenerCount != eventCount) {
                LOG.error("Listener: {}/{} ({}%)", listenerCount, eventCount, (int) (listenerCount * 100.0 / eventCount));
            }
        }
    }

    /**
     * A subscriber that counts the number of events it receives.
     */
    static class LoadSubscriber {

        private final long expectedTotal;
        private final AtomicLong counter = new AtomicLong();
        private final CountDownLatch latch;
        private final int eventDelayMs;

        LoadSubscriber(EventRouter<String> eventRouter, CountDownLatch latch, int expectedTotal, int eventDelayMs) {
            this.latch = latch;
            this.expectedTotal = expectedTotal;
            this.eventDelayMs = eventDelayMs;
            // Self-register. A nice feature most other event buses don't have.
            eventRouter.subscribe(this::onStringEvent);
        }

        private void onStringEvent(String strValue) {
            if (counter.incrementAndGet() == expectedTotal) latch.countDown();
            EventUtils.sleep(eventDelayMs);
        }

        public long getCounter() {
            return counter.get();
        }
    }

    /**
     * Adapts a {@link ModifiableEventRouter} to an {@link EventRouter}
     * for use in our load test.
     */
    static class ModifiableEventRouterAdapter<T> implements EventRouter<T> {
        private final ModifiableEventRouter<T> modifiableEventRouter;

        ModifiableEventRouterAdapter(ModifiableEventRouter<T> modifiableEventRouter) {
            this.modifiableEventRouter = modifiableEventRouter;
        }

        @Override
        public UUID subscribe(CheckedConsumer<T> eventHandler) {
            return modifiableEventRouter.subscribe(event -> {
                eventHandler.unchecked().accept(event);
                return event;
            });
        }

        @Override
        public void publish(T eventObj) {
            modifiableEventRouter.publish(eventObj);
        }

        @Override
        public void unsubscribe(UUID subscriptionId) {
            modifiableEventRouter.unsubscribe(subscriptionId);
        }

        @Override
        public Collection<UUID> getSubscriptions() {
            return modifiableEventRouter.getSubscriptions();
        }

        @Override
        public ThreadPoolExecutor getThreadPool() {
            return modifiableEventRouter.getThreadPool();
        }
    }
}
