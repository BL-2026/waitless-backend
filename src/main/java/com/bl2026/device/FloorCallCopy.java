package com.bl2026.device;

import com.bl2026.request.PaymentMethod;
import com.bl2026.request.RequestType;

import java.util.Locale;
import java.util.Map;

/**
 * User-facing push copy lives here — not inline in the FCM sender — so every
 * locale is explicit and the notifier stays about delivery, not wording.
 */
final class FloorCallCopy {

    private static final String DEFAULT_LOCALE = "fr";

    private record Bundle(
            String title,
            String calling,
            String bill,
            String billWithMethod,
            String cash,
            String card) {
    }

    private static final Map<String, Bundle> BY_LOCALE = Map.of(
            "en", new Bundle(
                    "Waitless",
                    "Table %d is calling",
                    "Table %d wants the bill",
                    "Table %d wants the bill · %s",
                    "Cash",
                    "Card"),
            "fr", new Bundle(
                    "Waitless",
                    "La table %d appelle",
                    "La table %d demande l'addition",
                    "La table %d demande l'addition · %s",
                    "Espèces",
                    "Carte"),
            "ar", new Bundle(
                    "Waitless",
                    "الطاولة %d تستدعي",
                    "الطاولة %d تطلب الحساب",
                    "الطاولة %d تطلب الحساب · %s",
                    "نقدًا",
                    "بطاقة"),
            "es", new Bundle(
                    "Waitless",
                    "La mesa %d está llamando",
                    "La mesa %d pide la cuenta",
                    "La mesa %d pide la cuenta · %s",
                    "Efectivo",
                    "Tarjeta")
    );

    private FloorCallCopy() {
    }

    static String title(String locale) {
        return bundle(locale).title();
    }

    static String body(String locale, RequestType type, int tableNumber, PaymentMethod paymentMethod) {
        Bundle copy = bundle(locale);
        if (type != RequestType.REQUEST_BILL) {
            return copy.calling().formatted(tableNumber);
        }
        if (paymentMethod == null) {
            return copy.bill().formatted(tableNumber);
        }
        String method = paymentMethod == PaymentMethod.CASH ? copy.cash() : copy.card();
        return copy.billWithMethod().formatted(tableNumber, method);
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
