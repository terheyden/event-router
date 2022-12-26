package com.terheyden.event;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * RunnableFunctionTest unit tests.
 */
public class RunnableFunctionTest {

    private static final Logger LOG = getLogger(RunnableFunctionTest.class);

    @Test
    public void justForCoverage() {

        RunnableFunction<String, String> runFunc = new RunnableFunction<>(() -> LOG.debug("ignore"));
        runFunc.apply("world");
    }
}
