package com.terheyden.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.System.out;

/**
 * ThreadPerSubscriberPublisherTest unit tests.
 */
public class ThreadPerSubscriberPublisherTest extends BaseThreadPoolTest {

    @Test
    public void test() {

        List<Integer> results = getResults();
        // The results should be sporadic.
        out.println(results);
    }

    @Override
    protected EventRouterConfig getConfig() {
        EventRouterConfig config = new EventRouterConfig();
        config.sendEventToSubscriberStrategy(new ThreadPoolSendStrategy(config.receivedEventHandlerThreadPool()));
        return config;
    }
}
