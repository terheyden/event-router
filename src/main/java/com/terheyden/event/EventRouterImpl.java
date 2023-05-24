package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import io.vavr.CheckedConsumer;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class EventRouterImpl<T> extends AbstractEventRouter<T> implements EventRouter<T> {

    EventRouterImpl(ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor, new ThreadPoolSendStrategy<>(threadPoolExecutor));
    }

    @Override
    public UUID subscribe(CheckedConsumer<T> eventHandler) {
        EventRouterSubscription<T> subscription = new EventRouterSubscription<>(eventHandler);
        getSubscriberManager().subscribe(subscription);
        return subscription.getSubscriptionId();
    }

    @Override
    public void unsubscribe(UUID subscriptionId) {
        getSubscriberManager().unsubscribe(subscriptionId);
    }

    @Override
    public void publish(T eventObj) {
        publishInternal(new EventRequest<>(eventObj));
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
