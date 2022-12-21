package com.terheyden.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.System.out;

/**
 * ThreadPerEventPublisherTest unit tests.
 */
public class ThreadPerEventPublisherTest extends BaseThreadPoolTest {

    @Override
    protected EventRouter createEventRouter() {

        return new EventRouter(new EventRouterConfig()
            .eventPublisher(new ThreadPerEventPublisher(THREADS)));
    }

    @Test
    public void test() {

        List<Integer> results = getResults();
        // The results should be in order, and increase in groups of 4.
        out.println(results);
    }
}
