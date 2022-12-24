package com.terheyden.event;

import java.util.Queue;
import java.util.UUID;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;

/**
 * EventRouter class.
 * Not static, so you can have multiple event routers.
 * You can always make it static if they want.
 */
class EventRouterSubscriber {

    /**
     * {@code [ Event.class : [ sub1, sub2, ... ] ]}.
     * {@code [ UUID : [ sub ] ]}.
     */
    private final EventSubscriberMap eventSubscriberMap = new EventSubscriberMap();

    /**
     * When an event of type {@code eventClass} is published, {@code eventHandler} will be called.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler will be subscribed to.
     * @return A UUID that can later be used to unsubscribe.
     */
    <T> UUID subscribe(Class<T> eventClass, CheckedConsumer<T> eventHandler) {
        // subscribe() expectes a consumer, but we store it as a function.
        ConsumerFunction1<Object, Object> eventFunc = consumerToFunction(eventHandler);
        return subscribeInternal(eventClass, eventFunc);
    }

    <T, R> UUID subscribeAndReply(Class<T> eventClass, CheckedFunction1<T, R> eventHandler) {
        return subscribeInternal(eventClass, eventHandler);
    }

    <T, R> UUID subscribeInternal(Object uuidClass, CheckedFunction1<T, R> eventHandler) {
        // Concurrent, so we don't need to synchronize.
        CheckedFunction1<Object, Object> eventFunc = functionToFunction(eventHandler);
        return eventSubscriberMap.add(uuidClass, eventFunc);
    }

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param eventClass Events are defined by their class type.
     *                   This is the type of event that the handler was subscribed to.
     * @param subscriptionId The UUID returned by the subscribe() method.
     * @return True if the subscription was found and removed.
     */
    <T> boolean unsubscribe(Class<T> eventClass, UUID subscriptionId) {
        // Concurrent, so we don't need to synchronize.
        return eventSubscriberMap.remove(eventClass, subscriptionId);
    }

    Queue<EventSubscription> findSubscribers(Object uuidClass) {
        return eventSubscriberMap.find(uuidClass);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConsumerFunction1<Object, Object> consumerToFunction(CheckedConsumer<T> eventHandler) {
        return new ConsumerFunction1<Object, Object>((CheckedConsumer) eventHandler);
    }

    @SuppressWarnings("unchecked")
    private static <T, R> CheckedFunction1<Object, Object> functionToFunction(CheckedFunction1<T, R> eventHandler) {
        return (CheckedFunction1<Object, Object>) eventHandler;
    }
}
