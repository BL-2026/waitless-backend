package com.bl2026.account;

import com.bl2026.auth.CurrentUser;
import com.bl2026.auth.FirebaseAuthenticationToken;
import com.bl2026.common.BadRequestException;
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
                .orElseGet(() -> accountRepository.save(build(principal, request)));
    }

    private Account build(FirebaseAuthenticationToken principal, CreateAccountRequest request) {
        String fullName = firstNonBlank(request == null ? null : request.fullName(), principal.getDisplayName());
        String email = firstNonBlank(request == null ? null : request.email(), principal.getEmail());

        // Both columns are NOT NULL, and Firebase email/password sign-up leaves displayName
        // empty, so the client has to send a name. Fail with 400 rather than a constraint violation.
        if (!StringUtils.hasText(fullName)) {
            throw new BadRequestException("fullName is required: the Firebase token carries no display name");
        }
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("email is required: the Firebase token carries no email");
        }

        return new Account(
                principal.getFirebaseUid(),
                fullName,
                request == null ? null : request.phoneArea(),
                request == null ? null : request.phoneNumber(),
                email);
    }

    /** The {@code Account} behind the current Firebase ID token. */
    @Transactional(readOnly = true)
    public Account requireCurrent() {
        String firebaseUid = CurrentUser.requireFirebaseUid();
        return accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new NotFoundException(
                        "No account for the current Firebase user; call POST /api/account first"));
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }
}
