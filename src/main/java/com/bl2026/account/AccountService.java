package com.bl2026.account;

import com.bl2026.auth.CurrentUser;
import com.bl2026.auth.FirebaseAuthenticationToken;
import com.bl2026.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Idempotent on {@code firebaseUid}: the Flutter app calls this on every login and only
     * the first call inserts a row.
     */
    @Transactional
    public Account registerCurrentUser(CreateAccountRequest request) {
        FirebaseAuthenticationToken principal = CurrentUser.require();
        return accountRepository.findByFirebaseUid(principal.getFirebaseUid())
                .orElseGet(() -> accountRepository.save(new Account(
                        principal.getFirebaseUid(),
                        firstNonBlank(request == null ? null : request.fullName(), principal.getDisplayName()),
                        firstNonBlank(request == null ? null : request.email(), principal.getEmail()))));
    }

    /** The {@code Account} behind the current Firebase ID token. */
    @Transactional(readOnly = true)
    public Account requireCurrent() {
        String firebaseUid = CurrentUser.requireFirebaseUid();
        return accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new NotFoundException(
                        "No account for the current Firebase user; call POST /api/accounts first"));
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }
}
