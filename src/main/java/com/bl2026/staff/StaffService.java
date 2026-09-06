package com.bl2026.staff;

import com.bl2026.common.NotFoundException;
import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffMemberRepository staffMemberRepository;
    private final StoreService storeService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffMember create(UUID storeId, CreateStaffRequest request) {
        Store store = storeService.requireOwnedStore(storeId);
        return staffMemberRepository.save(
                new StaffMember(store, request.fullName(), passwordEncoder.encode(request.pin())));
    }

    @Transactional(readOnly = true)
    public List<StaffMember> listForOwnedStore(UUID storeId) {
        storeService.requireOwnedStore(storeId);
        return staffMemberRepository.findByStoreIdOrderByFullNameAsc(storeId);
    }

    @Transactional(readOnly = true)
    public VerifyPinResponse verifyPin(UUID staffId, VerifyPinRequest request) {
        StaffMember staff = requireOwnedStaff(staffId);
        boolean valid = passwordEncoder.matches(request.pin(), staff.getPinHash());
        return valid
                ? new VerifyPinResponse(true, staff.getId(), staff.getFullName())
                : new VerifyPinResponse(false, null, null);
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
