package com.terheyden.event;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * If event {@code MyEvent} is published and there are 3 subscribers,
 * then 3 sendEventToSubscribers tasks are created and run on the thread pool in this publisher.
 */
class EventQuerySendStrategy<I, O> implements SendEventToSubscriberStrategy<I> {

    private static final Logger LOG = getLogger(EventQuerySendStrategy.class);

    private final ThreadPoolExecutor threadpool;

    public EventQuerySendStrategy(ThreadPoolExecutor threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEventToSubscribers(EventRequest<I> eventRequest, Collection<EventSubscription> subscribers) {

        subscribers
            .stream()
            .map(sub -> (EventQuerySubscription<I, O>) sub)
            .forEach(sub ->
            threadpool.execute(() -> executeQueryEventResponse(eventRequest, sub)));
    }

    private static <I, O> void executeQueryEventResponse(
        EventRequest<I> eventRequest,
        EventQuerySubscription<I, O> sub) {

        QueryEventRequest<I, O> queryRequest = (QueryEventRequest<I, O>) eventRequest;

        LOG.debug("Sending event query to subscriber.");
        O queryResponse = sub.getEventHandler().unchecked().apply(queryRequest.getEventObj());

        LOG.debug("Sending query response back to caller: {}", queryResponse);
        queryRequest.getCallback().unchecked().accept(queryResponse);
    }

    @Override
    public String getMetrics() {
        return EventUtils.threadReport(threadpool);
    }

    public ThreadPoolExecutor getThreadpool() {
        return threadpool;
    }
}
