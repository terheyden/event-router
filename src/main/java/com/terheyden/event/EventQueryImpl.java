package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class EventQueryImpl<I, O> extends AbstractEventRouter<I> implements EventQuery<I, O> {

    EventQueryImpl(ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor, new EventQuerySendStrategy<>(threadPoolExecutor));
    }

    @Override
    public UUID subscribe(CheckedFunction1<I, O> eventHandler) {
        EventQuerySubscription<I, O> subscription = new EventQuerySubscription<>(eventHandler);
        getSubscriberManager().subscribe(subscription);
        return subscription.getSubscriptionId();
    }

    @Override
    public void unsubscribe(UUID subscriptionId) {
        getSubscriberManager().unsubscribe(subscriptionId);
    }

    @Override
    public void query(I eventObj, CheckedConsumer<O> responseHandler) {
        publishInternal(new QueryEventRequest<>(eventObj, responseHandler));
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return super.getThreadPool();
    }

    @Override
    public Collection<UUID> getSubscriptions() {
        return getSubscribers()
            .stream()
            .map(EventSubscription::getSubscriptionId)
            .collect(Collectors.toList());
    }
}
