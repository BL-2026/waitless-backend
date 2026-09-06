package com.bl2026.request;

/** Envelope pushed over STOMP so clients can react without re-fetching. */
public record ServiceRequestEvent(Type event, ServiceRequestResponse request) {

    public enum Type {
        CREATED,
        ACKNOWLEDGED,
        RESOLVED
    }
}
