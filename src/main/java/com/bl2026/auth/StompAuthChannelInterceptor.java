package com.bl2026.auth;

import com.bl2026.common.NotFoundException;
import com.bl2026.request.RequestTopics;
import com.bl2026.store.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticates STOMP CONNECT with a Firebase ID token and scopes SUBSCRIBE to stores
 * the account owns. The HTTP upgrade itself stays open so clients can reach {@code /ws};
 * without a valid CONNECT they never see any topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern STORE_TOPIC = Pattern.compile(RequestTopics.STORE_REQUESTS_PATTERN);

    private final FirebaseBearerAuthenticator bearerAuthenticator;
    private final StoreService storeService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        return switch (accessor.getCommand()) {
            case CONNECT -> onConnect(message, accessor);
            case SUBSCRIBE -> onSubscribe(message, accessor);
            default -> message;
        };
    }

    private Message<?> onConnect(Message<?> message, StompHeaderAccessor accessor) {
        String header = firstHeader(accessor, FirebaseBearerAuthenticator.authorizationHeaderName());
        FirebaseBearerAuthenticator.Outcome outcome = bearerAuthenticator.authenticateHeader(header);

        if (!outcome.isOk()) {
            throw new MessagingException(switch (outcome.failure()) {
                case MISSING -> "Missing Firebase ID token on STOMP CONNECT";
                case FIREBASE_DISABLED -> "Firebase authentication is not configured on this server";
                case INVALID -> "Invalid or expired Firebase ID token";
            });
        }

        accessor.setUser(outcome.authentication());
        return message;
    }

    private Message<?> onSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof FirebaseAuthenticationToken authentication)) {
            throw new MessagingException("STOMP subscription requires an authenticated CONNECT");
        }

        String destination = accessor.getDestination();
        Matcher matcher = destination == null ? null : STORE_TOPIC.matcher(destination);
        if (matcher == null || !matcher.matches()) {
            throw new MessagingException("Unsupported STOMP destination: " + destination);
        }

        UUID storeId = UUID.fromString(matcher.group(1));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            storeService.requireOwnedStore(storeId);
        } catch (NotFoundException | IllegalStateException ex) {
            throw new MessagingException("Not allowed to subscribe to store " + storeId, ex);
        } finally {
            SecurityContextHolder.clearContext();
        }

        return message;
    }

    private static String firstHeader(StompHeaderAccessor accessor, String name) {
        String value = accessor.getFirstNativeHeader(name);
        if (value != null) {
            return value;
        }
        // Some STOMP clients lower-case native headers.
        return accessor.getFirstNativeHeader(name.toLowerCase());
    }
}
