package com.terheyden.event;

/**
 * EventRouterPublishException class.
 */
public class EventRouterPublishException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The subscriber that choked.
     */
    private final EventSubscription subscriber;

    /**
     * The event that was being published.
     */
    private final Object event;

    public EventRouterPublishException(EventSubscription subscriber, Object event, String message, Throwable cause) {
        super(message, cause);
        this.subscriber = subscriber;
        this.event = event;
    }

    public EventRouterPublishException(EventSubscription subscriber, Object event, Throwable cause) {
        super(cause);
        this.subscriber = subscriber;
        this.event = event;
    }

    public EventRouterPublishException(EventSubscription subscriber, Object event, String message) {
        super(message);
        this.subscriber = subscriber;
        this.event = event;
    }

    public EventSubscription getSubscriber() {
        return subscriber;
    }

    public Object getEvent() {
        return event;
    }
}
