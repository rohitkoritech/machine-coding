package com.interview.lld.tokenmanager;

import java.time.Instant;

public record Token(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}