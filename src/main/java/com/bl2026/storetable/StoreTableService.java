package com.bl2026.storetable;

import com.bl2026.common.NotFoundException;
import com.bl2026.menu.MenuItemResponse;
import com.bl2026.menu.MenuItemService;
import com.bl2026.store.Store;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreTableService {

    private final StoreTableRepository storeTableRepository;
    private final StoreService storeService;
    private final MenuItemService menuItemService;
    private final QrTokenGenerator qrTokenGenerator;

    @Transactional
    public StoreTable create(UUID storeId, CreateTableRequest request) {
        Store store = storeService.requireOwnedStore(storeId);
        return storeTableRepository.save(
                new StoreTable(store, request.tableNumber(), request.zone(), qrTokenGenerator.generate()));
    }

    @Transactional(readOnly = true)
    public List<StoreTable> listForOwnedStore(UUID storeId) {
        storeService.requireOwnedStore(storeId);
        return storeTableRepository.findByStoreIdOrderByTableNumberAsc(storeId);
    }

    /** Unauthenticated lookup by QR token. */
    @Transactional(readOnly = true)
    public StoreTable requireByQrToken(String qrToken) {
        return storeTableRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new NotFoundException("Unknown table QR token"));
    }

    @Transactional(readOnly = true)
    public TableResolutionResponse resolve(String qrToken) {
        StoreTable table = requireByQrToken(qrToken);
        Store store = table.getStore();
        List<MenuItemResponse> menu = menuItemService.listPublic(store.getId()).stream()
                .map(MenuItemResponse::from)
                .toList();
        return new TableResolutionResponse(
                new TableResolutionResponse.TableInfo(table.getId(), table.getTableNumber(), table.getZone()),
                new TableResolutionResponse.StoreInfo(store.getId(), store.getName()),
                menu);
    }
}
