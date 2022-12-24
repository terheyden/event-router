package com.terheyden.event;

/**
 * Used to configure a new {@link EventRouter}.
 */
public class EventRouterConfig {

    private EventPublisher eventPublisher = new DirectPublisher();
    private EventPublisher eventPublisherAsync = new ThreadPerEventPublisher(1000);

    public EventPublisher eventPublisher() {
        return eventPublisher;
    }

    public EventRouterConfig eventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public EventPublisher eventPublisherAsync() {
        return eventPublisherAsync;
    }

    public EventRouterConfig eventPublisherAsync(EventPublisher eventPublisherAsync) {
        this.eventPublisherAsync = eventPublisherAsync;
        return this;
    }
}
