package com.terheyden.event;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A classic publish-subscribe event router.
 * Sends events to all subscribers using a thread pool.
 *
 * @see EventRouter
 * @see EventQuery
 * @see ModifiableEventRouter
 */
public interface EventSubscriber {

    /**
     * Unsubscribe a previously-subscribed handler by its UUID.
     *
     * @param subscriptionId The UUID returned by the subscribe() method.
     */
    void unsubscribe(UUID subscriptionId);

    /**
     * Get a read-only list of all event subscriptions.
     */
    Collection<UUID> getSubscriptions();

    /**
     * Get the thread pool used by this event router and its components.
     * For reporting only; don't use this to schedule events.
     */
    ThreadPoolExecutor getThreadPool();
}
