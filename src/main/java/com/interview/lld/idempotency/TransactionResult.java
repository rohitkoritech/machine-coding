package com.interview.lld.idempotency;

public record TransactionResult(
        String transactionId,
        Status status,
        String message) {

    public enum Status {
        SUCCESS,
        FAILED
    }
}