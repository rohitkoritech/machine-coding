package com.interview.lld.tokenmanager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class TokenManager {

    private final TokenProvider tokenProvider;
    private final Clock clock;

    // Refresh slightly before the actual expiry time.
    // This prevents us from sending a token that is about to expire.
    private final Duration refreshBuffer;

    // The currently cached token.
    //
    // volatile is important because multiple threads can call getToken().
    // It guarantees that a thread sees the latest token after another
    // thread has refreshed it.
    private volatile Token currentToken;

    // Only one thread should refresh the token at a time.
    private final Object refreshLock = new Object();

    public TokenManager(
            TokenProvider tokenProvider,
            Clock clock,
            Duration refreshBuffer) {

        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.clock = Objects.requireNonNull(clock);
        this.refreshBuffer = Objects.requireNonNull(refreshBuffer);
    }

    /**
     * Returns a valid access token.
     *
     * Fast path:
     *   If the cached token is still valid, return it immediately.
     *
     * Slow path:
     *   If the token is missing/expired, refresh it.
     *
     * Thread safety:
     *   Only one thread can perform the refresh.
     */
    public String getToken() {

        // 1. Fast path.
        //
        // Most requests should come through here.
        Token token = currentToken;

        if (isValid(token)) {
            return token.accessToken();
        }

        // 2. Token needs to be refreshed.
        //
        // Multiple threads may arrive here simultaneously,
        // but only one can enter this block at a time.
        synchronized (refreshLock) {

            // 3. Double-check.
            //
            // Another thread may have refreshed the token
            // while this thread was waiting for the lock.
            token = currentToken;

            if (isValid(token)) {
                return token.accessToken();
            }

            // 4. Nobody has refreshed it yet.
            //
            // Use the refresh token from the previous token.
            String refreshToken = token == null
                    ? null
                    : token.refreshToken();

            Token newToken = tokenProvider.refresh(refreshToken);

            // 5. Publish the new token.
            currentToken = newToken;

            return newToken.accessToken();
        }
    }

    /**
     * Determines whether the cached token can still be used.
     *
     * A token is considered invalid when:
     * - there is no token, or
     * - it has reached the refresh buffer.
     */
    private boolean isValid(Token token) {

        if (token == null) {
            return false;
        }

        Instant refreshTime =
                token.expiresAt().minus(refreshBuffer);

        return clock.instant().isBefore(refreshTime);
    }
}