package com.bl2026.device;

import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private static final String DEFAULT_LOCALE = "fr";
    private static final Set<String> SUPPORTED = Set.of("en", "fr", "ar", "es");

    private final DeviceTokenRepository deviceTokenRepository;
    private final StoreService storeService;

    @Transactional
    public void register(UUID storeId, String token, String locale) {
        Store store = storeService.requireOwnedStore(storeId);
        String normalized = normalizeLocale(locale);
        deviceTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existing -> existing.reassign(store, normalized),
                        () -> deviceTokenRepository.save(new DeviceToken(store, token, normalized)));
    }

    @Transactional
    public void unregister(UUID storeId, String token) {
        storeService.requireOwnedStore(storeId);
        deviceTokenRepository.findByToken(token)
                .filter(row -> row.getStore().getId().equals(storeId))
                .ifPresent(deviceTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<DeviceToken> forStore(UUID storeId) {
        return deviceTokenRepository.findByStoreId(storeId);
    }

    @Transactional
    public void dropToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }

    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        String normalized = locale.trim().toLowerCase(Locale.ROOT);
        int dash = normalized.indexOf('-');
        if (dash > 0) {
            normalized = normalized.substring(0, dash);
        }
        return SUPPORTED.contains(normalized) ? normalized : DEFAULT_LOCALE;
    }
}
