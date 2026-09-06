package com.bl2026.menu;

import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final StoreService storeService;

    @Transactional
    public MenuItem create(UUID storeId, CreateMenuItemRequest request) {
        Store store = storeService.requireOwnedStore(storeId);
        return menuItemRepository.save(new MenuItem(store, request.category(), request.name(),
                request.description(), request.price()));
    }

    @Transactional(readOnly = true)
    public List<MenuItem> listForOwnedStore(UUID storeId) {
        storeService.requireOwnedStore(storeId);
        return menuItemRepository.findByStoreIdOrderByCategoryAscNameAsc(storeId);
    }

    /** Unauthenticated read used by the customer app after scanning a QR code. */
    @Transactional(readOnly = true)
    public List<MenuItem> listPublic(UUID storeId) {
        return menuItemRepository.findByStoreIdOrderByCategoryAscNameAsc(storeId);
    }
}
