package com.interview.lld.moneytransfer;

public record TransferResult(
    boolean success,
    String transferId,
    String errorCode,
    String message
) {}