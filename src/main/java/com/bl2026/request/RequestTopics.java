package com.bl2026.request;

import java.util.UUID;

/** Single place for STOMP destinations so publisher and auth stay in lockstep. */
public final class RequestTopics {

    private static final String STORE_REQUESTS = "/topic/stores/%s/requests";

    private RequestTopics() {
    }

    public static String storeRequests(UUID storeId) {
        return STORE_REQUESTS.formatted(storeId);
    }

    /** Pattern used by the STOMP interceptor to parse and authorize subscriptions. */
    public static final String STORE_REQUESTS_PATTERN =
            "^/topic/stores/([0-9a-fA-F-]{36})/requests$";
}
