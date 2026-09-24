package com.interview.lld.tokenmanager;

import java.time.Instant;
import java.util.UUID;

public class OAuthTokenProvider implements TokenProvider {

    private static final long TOKEN_TTL_SECONDS = 12 * 60 * 60;

    @Override
    public Token refresh(String refreshToken) {

        /*
         * In production:
         *
         * POST /oauth/token
         *
         * grant_type=refresh_token
         * refresh_token=<refreshToken>
         *
         * Then parse the response.
         */

        String accessToken =
                "access-" + UUID.randomUUID();

        String newRefreshToken =
                "refresh-" + UUID.randomUUID();

        Instant expiresAt =
                Instant.now().plusSeconds(TOKEN_TTL_SECONDS);

        return new Token(
                accessToken,
                newRefreshToken,
                expiresAt
        );
    }
}