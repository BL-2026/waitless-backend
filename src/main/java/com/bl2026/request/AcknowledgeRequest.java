package com.bl2026.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcknowledgeRequest(@NotNull UUID staffId) {
}
