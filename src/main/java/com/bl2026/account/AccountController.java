package com.bl2026.account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** Create the Account account on first login. Safe to call on every login. */
    @PostMapping
    public AccountResponse create(@RequestBody(required = false) CreateAccountRequest request) {
        return AccountResponse.from(accountService.registerCurrentUser(request));
    }

    @GetMapping("/me")
    public AccountResponse me() {
        return AccountResponse.from(accountService.requireCurrent());
    }
}
