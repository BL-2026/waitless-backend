package com.bl2026.request;

/**
 * Raised inside a transaction after a service request changes. The broadcaster listens
 * {@link org.springframework.transaction.event.TransactionPhase#AFTER_COMMIT} so phones
 * never see an event for a change that later rolled back.
 */
public record ServiceRequestChangedEvent(
        ServiceRequestEvent.Type type,
        ServiceRequestResponse request) {
}
