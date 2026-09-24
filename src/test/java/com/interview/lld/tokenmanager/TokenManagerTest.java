package com.interview.lld.tokenmanager;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TokenManagerTest {

    @Test
    void shouldReturnCachedToken() {

        TokenProvider provider = mock(TokenProvider.class);

        Token token = new Token(
                "token-123",
                "refresh-123",
                Instant.now().plusSeconds(3600)
        );

        when(provider.refresh(null))
                .thenReturn(token);

        TokenManager manager = new TokenManager(
                provider,
                Clock.systemUTC(),
                Duration.ZERO
        );

        // First call -> provider is called.
        String token1 = manager.getToken();

        // Second call -> cached token is returned.
        String token2 = manager.getToken();

        assertEquals("token-123", token1);
        assertEquals("token-123", token2);

        // Only one refresh should happen.
        verify(provider, times(1))
                .refresh(null);
    }


    @Test
    void shouldRefreshOnlyOnceForMultipleThreads() throws Exception {

        TokenProvider provider = mock(TokenProvider.class);

        Token token = new Token(
                "token-123",
                "refresh-123",
                Instant.now().plusSeconds(3600)
        );

        when(provider.refresh(null))
                .thenAnswer(invocation -> {
                    // Make refresh slow so threads overlap.
                    Thread.sleep(100);
                    return token;
                });

        TokenManager manager = new TokenManager(
                provider,
                Clock.systemUTC(),
                Duration.ZERO
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(10);

        List<Future<String>> futures = new ArrayList<>();

        // Start 10 concurrent calls.
        for (int i = 0; i < 10; i++) {
            futures.add(
                    executor.submit(manager::getToken)
            );
        }

        // Every thread should receive the same token.
        for (Future<String> future : futures) {
            assertEquals("token-123", future.get());
        }

        // Most important assertion:
        // only ONE thread should refresh the token.
        verify(provider, times(1))
                .refresh(null);

        executor.shutdown();
    }
}