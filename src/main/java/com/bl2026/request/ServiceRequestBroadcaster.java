package com.bl2026.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes service-request changes to {@link RequestTopics#storeRequests}.
 * Subscribers must CONNECT with a Firebase ID token and may only subscribe to stores
 * they own — see {@link com.bl2026.auth.StompAuthChannelInterceptor}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceRequestBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onServiceRequestChanged(ServiceRequestChangedEvent event) {
        String destination = RequestTopics.storeRequests(event.request().storeId());
        ServiceRequestEvent payload = new ServiceRequestEvent(event.type(), event.request());

        log.debug("Broadcasting {} for request {} to {}",
                event.type(), event.request().id(), destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}
