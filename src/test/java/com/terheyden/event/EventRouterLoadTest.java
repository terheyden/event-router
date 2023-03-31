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

    @Test
    @Disabled
    void testDefaultConfig() throws InterruptedException {
        runLoadTest(new EventRouterConfig());
    }

    @Test
    @Disabled
    void testIOConfig() throws InterruptedException {

        // Let's say it's a high-throughput system that is IO-bound (makes external resource calls).
        // 1000 threads would be pretty reasonable on a modern JVM with plenty left over for other concerns.
        EventRouterConfig config = new EventRouterConfig()
            .receivedEventHandlerThreadPool(ThreadPools.newDynamicThreadPool(1000))
//            .sendEventToSubscriberStrategy(new ThreadPoolSendStrategy(ThreadPools.newDynamicThreadPool(1000)));
            .sendEventToSubscriberStrategy(new SequentialSendStrategy());

        runLoadTest(config);
    }

    private void runLoadTest(EventRouterConfig config) throws InterruptedException {

        EventRouter eventRouter = new EventRouter(config);

        // Let's see how fast we can chew through these events.
        // The number delivered is: eventCount * subscriberCount.
        int eventCount = 3_000_000;
        // Most events would have maybe a handful of subscribers... we'll use 100.
        int subscriberCount = 100;
        // We'll use a CountDownLatch to wait for all subscribers to finish.
        CountDownLatch latch = new CountDownLatch(subscriberCount);

        LOG.info("Adding {} subscribers...", subscriberCount);
        List<LoadSubscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < subscriberCount; i++) {
            subscribers.add(new LoadSubscriber(eventRouter, latch, eventCount));
        }

        LOG.info("Publishing {} events...", eventCount);
        // Make our event payload objects.
        Integer intEvent = 1;
        Float floatEvent = 1.0F;
        String stringEvent = "hi";
        Long longEvent = 1L;
        long start = System.currentTimeMillis();

        for (int i = 0; i < eventCount; i++) {
            // Send different payloads to better simulate real activity.
            if (i % 2 == 0) eventRouter.publish(floatEvent);
            else if (i % 3 == 0) eventRouter.publish(intEvent);
            else if (i % 5 == 0) eventRouter.publish(stringEvent);
            else eventRouter.publish(longEvent);
        }

        LOG.info("Waiting for subscribers to finish...");
        latch.await(10, TimeUnit.SECONDS);
        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        long totalDelivered = eventCount * subscriberCount;
        DecimalFormat df = new DecimalFormat("#.00");
        LOG.info("Done in {} secs ({} msg/sec)", seconds, df.format(totalDelivered / seconds));
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

        LoadSubscriber(EventRouter eventRouter, CountDownLatch latch, int expectedTotal) {
            this.latch = latch;
            this.expectedTotal = expectedTotal;
            // Self-register. A nice feature most other event buses don't have.
            eventRouter.subscribe(Integer.class, this::onIntegerEvent);
            eventRouter.subscribe(Long.class, this::onLongEvent);
            eventRouter.subscribe(String.class, this::onStringEvent);
            eventRouter.subscribe(Float.class, this::onFloatEvent);
        }

        // Simulate many different event types.

        private void onIntegerEvent(Integer value) {
            if (counter.incrementAndGet() == expectedTotal) latch.countDown();
        }

        private void onLongEvent(Long value) {
            if (counter.incrementAndGet() == expectedTotal) latch.countDown();
        }

        private void onStringEvent(String strValue) {
            if (counter.incrementAndGet() == expectedTotal) latch.countDown();
        }

        private void onFloatEvent(Float value) {
            if (counter.incrementAndGet() == expectedTotal) latch.countDown();
        }

        public long getCounter() {
            return counter.get();
        }
    }
}
