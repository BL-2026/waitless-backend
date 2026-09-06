package com.bl2026.menu;

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

@RestController
@RequestMapping("/api/stores/{storeId}/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@PathVariable UUID storeId, @Valid @RequestBody CreateMenuItemRequest request) {
        return MenuItemResponse.from(menuItemService.create(storeId, request));
    }

    @GetMapping
    public List<MenuItemResponse> list(@PathVariable UUID storeId) {
        return menuItemService.listForOwnedStore(storeId).stream().map(MenuItemResponse::from).toList();
    }
}
