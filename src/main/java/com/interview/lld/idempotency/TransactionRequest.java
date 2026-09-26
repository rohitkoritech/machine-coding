package com.interview.lld.idempotency;

import java.math.BigDecimal;

public record TransactionRequest(
        String fromAccount,
        String toAccount,
        BigDecimal amount) {

    /*
     * Represents the logical contents of the request.
     *
     * Two requests with the same idempotency key but
     * different values must be rejected.
     */
    public String fingerprint() {
        return fromAccount
                + "|" + toAccount
                + "|" + amount;
    }
}