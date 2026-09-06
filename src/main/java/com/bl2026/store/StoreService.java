package com.bl2026.store;

import com.bl2026.account.Account;
import com.bl2026.account.AccountService;
import com.bl2026.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final AccountService accountService;

    @Transactional
    public Store create(CreateStoreRequest request) {
        Account account = accountService.requireCurrent();
        return storeRepository.save(new Store(account, request.name()));
    }

    @Transactional(readOnly = true)
    public List<Store> listForCurrentAccount() {
        return storeRepository.findByAccountIdOrderByCreatedAtAsc(accountService.requireCurrent().getId());
    }

    /**
     * Loads a store only if it belongs to the currently authenticated account. Every
     * {@code /api/stores/{storeId}/**} endpoint goes through here, which is what stops one
     * account from reading or mutating another account's data.
     */
    @Transactional(readOnly = true)
    public Store requireOwnedStore(UUID storeId) {
        UUID accountId = accountService.requireCurrent().getId();
        return storeRepository.findByIdAndAccountId(storeId, accountId)
                .orElseThrow(() -> new NotFoundException("Store " + storeId + " not found"));
    }
}
