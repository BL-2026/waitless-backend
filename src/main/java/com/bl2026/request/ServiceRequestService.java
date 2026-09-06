package com.bl2026.request;

import com.bl2026.common.BadRequestException;
import com.bl2026.common.ForbiddenException;
import com.bl2026.common.NotFoundException;
import com.bl2026.staff.StaffMember;
import com.bl2026.staff.StaffService;
import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import com.bl2026.storetable.StoreTable;
import com.bl2026.storetable.StoreTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final StoreTableService storeTableService;
    private final StoreService storeService;
    private final StaffService staffService;
    private final ServiceRequestBroadcaster broadcaster;

    /** Called by the customer web app with no authentication; the QR token is the only credential. */
    @Transactional
    public ServiceRequestResponse create(CreateServiceRequestRequest request) {
        validatePaymentMethod(request);

        StoreTable table = storeTableService.requireByQrToken(request.qrToken());
        Store store = table.getStore();

        ServiceRequest serviceRequest = serviceRequestRepository.save(
                new ServiceRequest(store, table, request.type(), request.paymentMethod()));

        broadcaster.broadcast(ServiceRequestEvent.Type.CREATED, serviceRequest);
        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> listActive(UUID storeId) {
        storeService.requireOwnedStore(storeId);
        return serviceRequestRepository
                .findByStoreIdAndStatusInOrderByCreatedAtAsc(storeId, RequestStatus.ACTIVE)
                .stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }

    @Transactional
    public ServiceRequestResponse acknowledge(UUID requestId, AcknowledgeRequest body) {
        ServiceRequest serviceRequest = requireOwnedRequest(requestId);
        if (serviceRequest.getStatus() != RequestStatus.OPEN) {
            throw new BadRequestException("Request is already " + serviceRequest.getStatus());
        }

        StaffMember staff = staffService.requireOwnedStaff(body.staffId());
        if (!staff.getStore().getId().equals(serviceRequest.getStore().getId())) {
            throw new ForbiddenException("Staff member belongs to a different store");
        }

        serviceRequest.acknowledge(staff);
        broadcaster.broadcast(ServiceRequestEvent.Type.ACKNOWLEDGED, serviceRequest);
        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse resolve(UUID requestId) {
        ServiceRequest serviceRequest = requireOwnedRequest(requestId);
        if (serviceRequest.getStatus() == RequestStatus.RESOLVED) {
            throw new BadRequestException("Request is already resolved");
        }

        serviceRequest.resolve();
        broadcaster.broadcast(ServiceRequestEvent.Type.RESOLVED, serviceRequest);
        return ServiceRequestResponse.from(serviceRequest);
    }

    private ServiceRequest requireOwnedRequest(UUID requestId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Service request " + requestId + " not found"));
        storeService.requireOwnedStore(serviceRequest.getStore().getId());
        return serviceRequest;
    }

    private void validatePaymentMethod(CreateServiceRequestRequest request) {
        if (request.type() == RequestType.REQUEST_BILL && request.paymentMethod() == null) {
            throw new BadRequestException("paymentMethod is required when type is REQUEST_BILL");
        }
        if (request.type() != RequestType.REQUEST_BILL && request.paymentMethod() != null) {
            throw new BadRequestException("paymentMethod is only allowed when type is REQUEST_BILL");
        }
    }
}
