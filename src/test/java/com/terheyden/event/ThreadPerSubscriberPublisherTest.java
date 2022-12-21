package com.terheyden.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.System.out;

/**
 * ThreadPerSubscriberPublisherTest unit tests.
 */
public class ThreadPerSubscriberPublisherTest extends BaseThreadPoolTest {

    @Override
    protected EventRouter createEventRouter() {

        return new EventRouter(new EventRouterConfig()
            .eventPublisher(new ThreadPerSubscriberPublisher(THREADS)));
    }

    @Test
    public void test() {

        List<Integer> results = getResults();
        // The results should be sporadic.
        out.println(results);
    }
}
