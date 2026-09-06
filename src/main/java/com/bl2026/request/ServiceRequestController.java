package com.bl2026.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    /** Public: called by the customer web app. See {@code PublicEndpoints}. */
    @PostMapping("/api/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequestResponse create(@Valid @RequestBody CreateServiceRequestRequest request) {
        return serviceRequestService.create(request);
    }

    @GetMapping("/api/stores/{storeId}/requests/active")
    public List<ServiceRequestResponse> listActive(@PathVariable UUID storeId) {
        return serviceRequestService.listActive(storeId);
    }

    @PostMapping("/api/requests/{requestId}/acknowledge")
    public ServiceRequestResponse acknowledge(@PathVariable UUID requestId,
                                              @Valid @RequestBody AcknowledgeRequest body) {
        return serviceRequestService.acknowledge(requestId, body);
    }

    @PostMapping("/api/requests/{requestId}/resolve")
    public ServiceRequestResponse resolve(@PathVariable UUID requestId) {
        return serviceRequestService.resolve(requestId);
    }
}
