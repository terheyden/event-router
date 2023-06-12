package com.terheyden.event;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * EventUtils class.
 */
/* package */ final class EventUtils {

    private EventUtils() {
        // Private since this class shouldn't be instantiated.
    }

    /**
     * Throw any exception unchecked.
     */
    @SuppressWarnings("unchecked")
    /* package */ static <E extends Throwable, R> R throwUnchecked(Throwable throwable) throws E {
        throw (E) throwable;
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throwUnchecked(e);
        }
    }

    /**
     * Extract useful stats from a thread pool.
     */
    public static String threadReport(ThreadPoolExecutor pool) {

        StringBuilder bui = new StringBuilder();

        bui.append("Pool thread size: ").append(pool.getPoolSize()).append('\n');
        bui.append("Active thread count: ").append(pool.getActiveCount()).append('\n');
        bui.append("Current queue size: ").append(pool.getQueue().size()).append('\n');
        bui.append("Total tasks scheduled: ").append(pool.getTaskCount()).append('\n');
        bui.append("Total completed tasks: ").append(pool.getCompletedTaskCount()).append('\n');

        return bui.toString();
    }
}
