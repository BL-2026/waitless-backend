package com.bl2026.staff;

import java.util.UUID;

public record StaffResponse(UUID id, String fullName) {

    public static StaffResponse from(StaffMember staff) {
        return new StaffResponse(staff.getId(), staff.getFullName());
    }
}
