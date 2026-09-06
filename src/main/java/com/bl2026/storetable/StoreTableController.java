package com.bl2026.storetable;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Account-facing table management. Requires a Firebase ID token. */
@RestController
@RequestMapping("/api/stores/{storeId}/tables")
@RequiredArgsConstructor
public class StoreTableController {

    private final StoreTableService storeTableService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreTableResponse create(@PathVariable UUID storeId, @Valid @RequestBody CreateTableRequest request) {
        return StoreTableResponse.from(storeTableService.create(storeId, request));
    }

    @GetMapping
    public List<StoreTableResponse> list(@PathVariable UUID storeId) {
        return storeTableService.listForOwnedStore(storeId).stream().map(StoreTableResponse::from).toList();
    }
}
