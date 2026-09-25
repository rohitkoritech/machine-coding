package com.interview.lld.retry;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryMechanismTest {

    @Test
    void testSuccessFlow() throws Exception {

        RetryMechanism retryMechanism =
                new RetryMechanism(5, 1000, 10_000);

        Boolean result = retryMechanism.execute(() -> {
            System.out.println("downstream service");
            return true;
        });

        assertTrue(result);
    }

    @Test
    void testFailureAfterMaxAttempts() throws Exception {

        RetryMechanism retryMechanism =
                new RetryMechanism(3, 10, 1000);

        AtomicInteger attempts = new AtomicInteger();

        assertThrows(
                RetryableException.class,
                () -> retryMechanism.execute(() -> {
                    attempts.incrementAndGet();
                    throw new RetryableException("Downstream failed");
                })
        );

        assertEquals(3, attempts.get());
    }

}