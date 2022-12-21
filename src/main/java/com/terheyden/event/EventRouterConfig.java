package com.terheyden.event;

/**
 * Used to configure a new {@link EventRouter}.
 */
public class EventRouterConfig {

    private EventPublisher eventPublisher = new DirectPublisher();

    public EventPublisher eventPublisher() {
        return eventPublisher;
    }

    public EventRouterConfig eventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }
}
