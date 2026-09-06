package com.bl2026.request;

import java.util.Set;

public enum RequestStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED;

    /** Statuses that still need staff attention; drives the active-requests endpoint. */
    public static final Set<RequestStatus> ACTIVE = Set.of(OPEN, ACKNOWLEDGED);
}
