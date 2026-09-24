package com.interview.lld.moneytransfer;

import java.math.BigDecimal;

interface TransferService {
    TransferResult transfer(
        String senderId,
        String recipientId,
        BigDecimal amount
    );
}