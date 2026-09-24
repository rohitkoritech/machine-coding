package com.interview.lld.moneytransfer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * For one JVM, I'd use synchronization. Across multiple service instances, I'd enforce concurrency at the persistence
 * layer using a database transaction and appropriate locking or optimistic concurrency control, plus idempotency for
 * retries.
 *
 * In-memory exercise
 * synchronized
 *
 * Real distributed service
 * @Transactional
 *         +
 * DB locking / @Version
 *         +
 * Idempotency
 *         +
 * Transfer/Ledger record
 */
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;

    public TransferServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public synchronized TransferResult transfer(
            String senderId,
            String recipientId,
            BigDecimal amount) {

        // 1. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return failure(
                    "INVALID_AMOUNT",
                    "Transfer amount must be greater than zero"
            );
        }

        // 2. Sender and recipient must be different
        if (senderId == null || recipientId == null) {
            return failure(
                    "INVALID_ACCOUNT",
                    "Sender and recipient are required"
            );
        }

        if (senderId.equals(recipientId)) {
            return failure(
                    "SAME_ACCOUNT",
                    "Sender and recipient cannot be the same"
            );
        }

        // 3. Find accounts
        Account sender = accountService.getAccount(senderId);
        Account recipient = accountService.getAccount(recipientId);

        if (sender == null) {
            return failure(
                    "SENDER_NOT_FOUND",
                    "Sender account does not exist"
            );
        }

        if (recipient == null) {
            return failure(
                    "RECIPIENT_NOT_FOUND",
                    "Recipient account does not exist"
            );
        }

        // 4. Validate account status
        if (!sender.isActive()) {
            return failure(
                    "SENDER_INACTIVE",
                    "Sender account is inactive"
            );
        }

        if (!recipient.isActive()) {
            return failure(
                    "RECIPIENT_INACTIVE",
                    "Recipient account is inactive"
            );
        }

        // 5. Check balance
        if (sender.getBalance().compareTo(amount) < 0) {
            return failure(
                    "INSUFFICIENT_FUNDS",
                    "Insufficient balance"
            );
        }

        // 6. Debit sender
        sender.setBalance(
                sender.getBalance().subtract(amount)
        );

        // 7. Credit recipient
        recipient.setBalance(
                recipient.getBalance().add(amount)
        );

        // 8. Return success
        return new TransferResult(
                true,
                UUID.randomUUID().toString(),
                null,
                "Transfer completed successfully"
        );
    }

    private TransferResult failure(
            String errorCode,
            String message) {

        return new TransferResult(
                false,
                null,
                errorCode,
                message
        );
    }
}