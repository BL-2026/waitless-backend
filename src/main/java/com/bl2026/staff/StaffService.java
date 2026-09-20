package com.bl2026.staff;

import com.bl2026.common.BadRequestException;
import com.bl2026.common.NotFoundException;
import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffMemberRepository staffMemberRepository;
    private final StoreService storeService;

    @Transactional
    public StaffMember create(UUID storeId, CreateStaffRequest request) {
        Store store = storeService.requireOwnedStore(storeId);

        if (staffMemberRepository.existsByStoreIdAndPin(storeId, request.pin())) {
            throw new BadRequestException("PIN " + request.pin() + " is already used in this venue");
        }

        return staffMemberRepository.save(new StaffMember(store, request.fullName(), request.pin()));
    }

    @Transactional(readOnly = true)
    public List<StaffMember> listForOwnedStore(UUID storeId) {
        storeService.requireOwnedStore(storeId);
        return staffMemberRepository.findByStoreIdOrderByFullNameAsc(storeId);
    }

    @Transactional(readOnly = true)
    public VerifyPinResponse verifyPin(UUID staffId, VerifyPinRequest request) {
        StaffMember staff = requireOwnedStaff(staffId);
        boolean valid = staff.getPin().equals(request.pin());
        return valid
                ? new VerifyPinResponse(true, staff.getId(), staff.getFullName())
                : new VerifyPinResponse(false, null, null);
    }

    @Transactional(readOnly = true)
    public VerifyPinResponse verifyPinForStore(UUID storeId, VerifyPinRequest request) {
        storeService.requireOwnedStore(storeId);

        return staffMemberRepository.findByStoreIdAndPin(storeId, request.pin())
                .map(staff -> new VerifyPinResponse(true, staff.getId(), staff.getFullName()))
                .orElseGet(() -> new VerifyPinResponse(false, null, null));
    }

    /** Loads a staff member only if their store belongs to the authenticated account. */
    @Transactional(readOnly = true)
    public StaffMember requireOwnedStaff(UUID staffId) {
        StaffMember staff = staffMemberRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff member " + staffId + " not found"));
        storeService.requireOwnedStore(staff.getStore().getId());
        return staff;
    }
}
