package com.bl2026.device;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/api/stores/{storeId}/device-tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@PathVariable UUID storeId, @Valid @RequestBody RegisterDeviceTokenRequest request) {
        deviceTokenService.register(storeId, request.token(), request.locale());
    }

    @DeleteMapping("/api/stores/{storeId}/device-tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable UUID storeId, @Valid @RequestBody UnregisterDeviceTokenRequest request) {
        deviceTokenService.unregister(storeId, request.token());
    }
}
