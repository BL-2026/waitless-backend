package com.bl2026.device;

import com.bl2026.request.RequestType;

import java.util.Locale;
import java.util.Map;

final class FloorCallCopy {

    private static final String DEFAULT_LOCALE = "fr";

    private record Bundle(String title, String calling, String bill) {
    }

    private static final Map<String, Bundle> BY_LOCALE = Map.of(
            "en", new Bundle("Waitless", "Table %d is calling", "Table %d wants the bill"),
            "fr", new Bundle("Waitless", "La table %d appelle", "La table %d demande l'addition"),
            "ar", new Bundle("Waitless", "الطاولة %d تستدعي", "الطاولة %d تطلب الحساب"),
            "es", new Bundle("Waitless", "La mesa %d está llamando", "La mesa %d pide la cuenta")
    );

    private FloorCallCopy() {
    }

    static String title(String locale) {
        return bundle(locale).title();
    }

    static String body(String locale, RequestType type, int tableNumber) {
        Bundle bundle = bundle(locale);
        String template = type == RequestType.REQUEST_BILL ? bundle.bill() : bundle.calling();
        return template.formatted(tableNumber);
    }

    private static Bundle bundle(String locale) {
        if (locale == null || locale.isBlank()) {
            return BY_LOCALE.get(DEFAULT_LOCALE);
        }
        String normalized = locale.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("-")) {
            normalized = normalized.substring(0, normalized.indexOf('-'));
        }
        return BY_LOCALE.getOrDefault(normalized, BY_LOCALE.get(DEFAULT_LOCALE));
    }
}
