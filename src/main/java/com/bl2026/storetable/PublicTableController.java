package com.bl2026.storetable;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Unauthenticated: this is the first call the customer web app makes after a QR scan. */
@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class PublicTableController {

    private final StoreTableService storeTableService;

    @GetMapping("/{qrToken}")
    public TableResolutionResponse resolve(@PathVariable String qrToken) {
        return storeTableService.resolve(qrToken);
    }
}
