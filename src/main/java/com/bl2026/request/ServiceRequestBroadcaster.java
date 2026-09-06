package com.bl2026.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes service-request changes to {@code /topic/stores/{storeId}/requests}.
 * Client wiring (subscriptions, auth on the STOMP handshake) is not built yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceRequestBroadcaster {

    private static final String DESTINATION_TEMPLATE = "/topic/stores/%s/requests";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(ServiceRequestEvent.Type eventType, ServiceRequest request) {
        UUID storeId = request.getStore().getId();
        String destination = DESTINATION_TEMPLATE.formatted(storeId);
        ServiceRequestEvent event = new ServiceRequestEvent(eventType, ServiceRequestResponse.from(request));

        log.debug("Broadcasting {} for request {} to {}", eventType, request.getId(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
