package com.interview.lld.idempotency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdempotencyHandlerTest {

    @Test
    void shouldProcessSuccessfully() {

        TransactionProcessor processor = (request) -> {
            String transactionId = UUID.randomUUID().toString();
            return new TransactionResult(transactionId, TransactionResult.Status.SUCCESS, "success");
        };

        IdempotencyHandler handler = new IdempotencyHandler(processor);

        TransactionResult result = handler.process(UUID.randomUUID().toString(),
                new TransactionRequest("rohit", "priya", new BigDecimal(100)));

        assertEquals("success", result.message());
    }

    @Test
    void shouldReturnSameResultAndProcessOnlyOnceForSameKey() {

        AtomicInteger processorCalls = new AtomicInteger();

        TransactionProcessor processor = request -> {
            processorCalls.incrementAndGet();

            return new TransactionResult(
                    "tx-123",
                    TransactionResult.Status.SUCCESS,
                    "success"
            );
        };

        IdempotencyHandler handler = new IdempotencyHandler(processor);

        String idempotencyKey = "abc-123";

        TransactionRequest request = new TransactionRequest(
                "rohit",
                "priya",
                new BigDecimal("100")
        );

        // First request
        TransactionResult first = handler.process(idempotencyKey, request);

        // Retry with same idempotency key
        TransactionResult second = handler.process(idempotencyKey, request);

        // Same result returned
        assertEquals(first, second);

        // Transaction processed only once
        assertEquals(1, processorCalls.get());
    }

}