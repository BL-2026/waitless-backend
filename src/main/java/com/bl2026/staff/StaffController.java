package com.bl2026.staff;

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
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/api/stores/{storeId}/staff")
    public List<StaffResponse> list(@PathVariable UUID storeId) {
        return staffService.listForOwnedStore(storeId).stream().map(StaffResponse::from).toList();
    }

    @PostMapping("/api/stores/{storeId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse create(@PathVariable UUID storeId, @Valid @RequestBody CreateStaffRequest request) {
        return StaffResponse.from(staffService.create(storeId, request));
    }

    @PostMapping("/api/staff/{staffId}/verify-pin")
    public VerifyPinResponse verifyPin(@PathVariable UUID staffId, @Valid @RequestBody VerifyPinRequest request) {
        return staffService.verifyPin(staffId, request);
    }
}
