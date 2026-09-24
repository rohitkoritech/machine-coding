package com.interview.lld.circuitbreaker;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CircuitBreaker {

    enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;
    private final int successThreshold;
    private final long openTimeoutMillis;

    private final AtomicReference<State> state =
            new AtomicReference<>(State.CLOSED);

    private final AtomicInteger failureCount =
            new AtomicInteger(0);

    private final AtomicInteger successCount =
            new AtomicInteger(0);

    private final AtomicLong openedAt =
            new AtomicLong(0);

    public CircuitBreaker(
            int failureThreshold,
            int successThreshold,
            Duration openTimeout) {

        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.openTimeoutMillis = openTimeout.toMillis();
    }

    public <T> T execute(Supplier<T> operation) {

        if (!allowRequest()) {
            throw new CircuitBreakerOpenException();
        }

        try {
            T result = operation.get();
            onSuccess();
            return result;

        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private boolean allowRequest() {

        State current = state.get();

        if (current == State.CLOSED) {
            return true;
        }

        if (current == State.OPEN) {

            long now = System.currentTimeMillis();
            long openedTime = openedAt.get();

            if (now - openedTime >= openTimeoutMillis) {

                // Only ONE thread gets permission to move OPEN -> HALF_OPEN.
                if (state.compareAndSet(
                        State.OPEN,
                        State.HALF_OPEN)) {

                    successCount.set(0);
                    failureCount.set(0);

                    return true;
                }
            }

            return false;
        }

        // HALF_OPEN
        // Allow only the thread that successfully transitioned
        // OPEN -> HALF_OPEN to probe.
        return false;
    }

    private void onSuccess() {

        State current = getState();

        if (current == State.CLOSED) {
            failureCount.set(0);
            return;
        }

        if (current == State.HALF_OPEN) {

            int successes = successCount.incrementAndGet();

            if (successes >= successThreshold) {

                if (state.compareAndSet(
                        State.HALF_OPEN,
                        State.CLOSED)) {

                    successCount.set(0);
                    failureCount.set(0);
                }
            }
        }
    }

    private void onFailure() {

        State current = state.get();

        if (current == State.CLOSED) {

            int failures = failureCount.incrementAndGet();

            if (failures >= failureThreshold) {

                if (state.compareAndSet(
                        State.CLOSED,
                        State.OPEN)) {

                    openedAt.set(System.currentTimeMillis());
                }
            }

        } else if (current == State.HALF_OPEN) {

            if (state.compareAndSet(
                    State.HALF_OPEN,
                    State.OPEN)) {

                openedAt.set(System.currentTimeMillis());
                successCount.set(0);
            }
        }
    }

    public State getState() {
        return state.get();
    }

    public static class CircuitBreakerOpenException
            extends RuntimeException {

        public CircuitBreakerOpenException() {
            super("Circuit breaker is OPEN");
        }
    }
}
