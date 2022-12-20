package com.terheyden.event;

/**
 * EventRouterBuilder class.
 */
public class EventRouterBuilder {

    private EventPublisher eventPublisher = new DirectPublisher();

    /**
     * Specify the event publishing strategy. For example, should events be published
     * on the calling thread, a single background thread, or a thread pool?
     * If not specified, the default is to publish events on the calling thread ({@link DirectPublisher}).
     */
    public EventRouterBuilder eventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public EventRouter build() {
        return new EventRouter(eventPublisher);
    }
}
