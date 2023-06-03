package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import io.vavr.CheckedFunction1;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
public class ModifiableEventRouterImpl<T> extends BaseEventRouter<T> implements ModifiableEventRouter<T> {

    ModifiableEventRouterImpl(SubscriberExceptionHandler eventHandler, ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor, new ModifiableEventSendStrategy<>(eventHandler));
    }

    @Override
    public UUID subscribe(CheckedFunction1<T, T> eventHandler) {
        ModifiableEventSubscription<T> subscription = new ModifiableEventSubscription<>(eventHandler);
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
