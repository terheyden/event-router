package com.terheyden.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.System.out;

/**
 * SequentialSendStrategyTest unit tests.
 */
public class SequentialSendStrategyTest extends BaseThreadPoolTest {

    @Test
    public void test() {

        List<Integer> results = getResults();
        // The results should be in order, and increase in groups of 4.
        out.println(results);
    }

    @Override
    protected EventRouterConfig getConfig() {
        return new EventRouterConfig();
    }
}
