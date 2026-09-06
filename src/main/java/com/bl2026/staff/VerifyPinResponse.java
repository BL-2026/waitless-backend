package com.bl2026.staff;

import java.util.UUID;

public record VerifyPinResponse(boolean valid, UUID staffId, String fullName) {
}
