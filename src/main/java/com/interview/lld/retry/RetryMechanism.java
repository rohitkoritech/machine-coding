package com.interview.lld.retry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class RetryMechanism {

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long maxDelayMillis;

    public RetryMechanism(
            int maxAttempts,
            long initialDelayMillis,
            long maxDelayMillis) {

        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
    }

    public <T> T execute(Supplier<T> operation)
            throws Exception {

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {
                return operation.get();

            } catch (Exception e) {

                lastException = e;

                // Don't retry non-retryable errors
                if (!isRetryable(e)) {
                    throw e;
                }

                // No more attempts
                if (attempt == maxAttempts) {
                    throw e;
                }

                long delay = calculateDelay(e, attempt);

                Thread.sleep(delay);
            }
        }

        throw lastException;
    }

    private boolean isRetryable(Exception e) {

        if (!(e instanceof HttpException httpException)) {
            return e instanceof RetryableException;
        }

        int status = httpException.getStatusCode();

        return status == 429
                || status == 500
                || status == 502
                || status == 503
                || status == 504;
    }

    private long calculateDelay(
            Exception e,
            int attempt) {

        // Server tells us when to retry.
        if (e instanceof HttpException httpException
                && httpException.getRetryAfterMillis() != null) {

            return Math.min(
                    httpException.getRetryAfterMillis(),
                    maxDelayMillis
            );
        }

        // Exponential backoff:
        // 1s → 2s → 4s → 8s
        long exponentialDelay =
                initialDelayMillis * (1L << (attempt - 1));

        long cappedDelay =
                Math.min(
                        exponentialDelay,
                        maxDelayMillis
                );

        // Full jitter:
        // random value between 0 and cappedDelay
        if (cappedDelay == 0) {
            return 0;
        }

        return ThreadLocalRandom.current()
                .nextLong(cappedDelay + 1);
    }
}