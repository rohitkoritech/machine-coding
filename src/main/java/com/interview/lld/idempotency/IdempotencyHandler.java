package com.interview.lld.idempotency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IdempotencyHandler {
    private final ConcurrentMap<String, StoredResult> results =
            new ConcurrentHashMap<>();

    private final TransactionProcessor processor;

    public IdempotencyHandler(TransactionProcessor processor) {
        this.processor = processor;
    }

    public TransactionResult process(
            String idempotencyKey,
            TransactionRequest request) {

        String fingerprint = request.fingerprint();
        StoredResult stored = results.compute(
                idempotencyKey,
                (key, existing) -> {

                    if (existing != null) {
                        if (!existing.fingerprint()
                                .equals(fingerprint)) {
                            throw new IdempotencyConflictException(
                                    "Idempotency key reused with " +
                                            "different request"
                            );
                        }
                        return existing;
                    }

                    TransactionResult result =
                            processor.process(request);

                    return new StoredResult(
                            fingerprint,
                            result
                    );
                }
        );

        return stored.result();
    }

    private record StoredResult(
            String fingerprint,
            TransactionResult result) {
    }
}