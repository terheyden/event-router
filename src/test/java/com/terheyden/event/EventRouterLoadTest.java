package com.terheyden.event;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * EventRouterLoadTest class.
 */
class EventRouterLoadTest {

    static final Logger LOG = getLogger(EventRouterLoadTest.class);

    /**
     * Test throughput of the default config, assuming it's a CPU-intensive system.
     */
    @Test
    @Disabled
    void testDefaultConfigForCPU() throws InterruptedException {
        int simulatedWorkDelayMs = 0; // We want to test throughput, so no simulated delay.
        int numberOfEvents = 50_000;
        runLoadTest(EventRouters.createWithEventType(String.class).build(), numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test throughput of the default config, assuming it's an IO-bound system.
     */
    @Test
    @Disabled
    void testDefaultConfigForIO() throws InterruptedException {
        int simulatedWorkDelayMs = 3; // Let's say each task takes 3ms to complete.
        int numberOfEvents = 10_000;
        runLoadTest(EventRouters.createWithEventType(String.class).build(), numberOfEvents, simulatedWorkDelayMs);
    }

    /**
     * Test optimized settings for an IO-bound system.
     */
    @Test
    @Disabled
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
        Integer intEvent = 1;
        Float floatEvent = 1.0F;
        String stringEvent = "hi";
        Long longEvent = 1L;
        long start = System.currentTimeMillis();

        for (int i = 0; i < eventCount; i++) {
            eventRouter.publish(stringEvent);
        }

        LOG.info("Waiting for subscribers to finish...");
        latch.await(10, TimeUnit.SECONDS);
        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        long totalDelivered = eventCount * subscriberCount;
        DecimalFormat df = new DecimalFormat("#.00");
        LOG.info("Done in {} secs ({} msg/sec)", seconds, df.format(totalDelivered / seconds));
        LOG.debug(EventUtils.threadReport(eventRouter.getThreadPool()));
        reportIncompleteListeners(subscribers, eventCount);
    }

    private static void reportIncompleteListeners(List<LoadSubscriber> listeners, int eventCount) {
        for (LoadSubscriber listener : listeners) {
            long listenerCount = listener.getCounter();
            if (listenerCount != eventCount) {
                LOG.error("Listener: {}", listenerCount);
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
}
