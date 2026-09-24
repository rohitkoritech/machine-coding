package com.interview.lld.ratelimiter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedWindowRateLimiterTest {

    @Test
    void shouldAllowOnlyFiveConcurrentRequests() throws Exception {

        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(5, 10_000);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Future<Boolean>> futures = new ArrayList<>();

        // 10 threads try to access the same client.
        for (int i = 0; i < 10; i++) {
            futures.add(
                    executor.submit(
                            () -> rateLimiter.allowRequest("client-1")
                    )
            );
        }

        int allowed = 0;

        for (Future<Boolean> future : futures) {
            if (future.get()) {
                allowed++;
            }
        }

        executor.shutdown();

        assertEquals(5, allowed);
    }

}