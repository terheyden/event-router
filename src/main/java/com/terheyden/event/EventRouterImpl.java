package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class EventRouterImpl<T> extends BaseEventRouter<T> implements EventRouter<T> {

    EventRouterImpl(ThreadPoolExecutor threadPoolExecutor, SendEventStrategy<T> sendStrategy) {
        super(threadPoolExecutor, sendStrategy);
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
