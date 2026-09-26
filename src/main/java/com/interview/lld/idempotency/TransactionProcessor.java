package com.interview.lld.idempotency;

public interface TransactionProcessor {
    TransactionResult process(TransactionRequest request);
}
