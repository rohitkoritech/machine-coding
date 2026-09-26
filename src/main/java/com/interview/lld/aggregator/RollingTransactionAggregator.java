package com.interview.lld.aggregator;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RollingTransactionAggregator {

    private static final Duration WINDOW = Duration.ofHours(24);
    private final TreeMap<Instant, List<Transaction>> transactions = new TreeMap<>();
    private final Map<String, Transaction> transactionIds = new java.util.HashMap<>();
    private BigDecimal total = BigDecimal.ZERO;
    private final Object lock = new Object();
    private final Clock clock;

    public RollingTransactionAggregator() {
        this(Clock.systemUTC());
    }

    public RollingTransactionAggregator(Clock clock) {
        this.clock = clock;
    }

    public void record(Transaction transaction) {

        synchronized (lock) {

            // 1. Ignore duplicate transaction.
            if (transactionIds.containsKey(transaction.id())) {
                return;
            }

            // 2. Store transaction by timestamp.
            transactions
                    .computeIfAbsent(
                            transaction.timestamp(),
                            key -> new ArrayList<>()
                    )
                    .add(transaction);

            // 3. Remember transaction ID.
            transactionIds.put(transaction.id(), transaction);

            // 4. Update running total.
            total = total.add(transaction.amount());

            // 5. Remove transactions older than 24 hours.
            removeExpired();
        }
    }

    public BigDecimal getTotal() {

        synchronized (lock) {

            // Remove expired transactions before calculating total.
            removeExpired();

            return total;
        }
    }

    private void removeExpired() {
        Instant cutoff = clock.instant().minus(WINDOW);
        while (!transactions.isEmpty()) {

            Map.Entry<Instant, List<Transaction>> oldest =
                    transactions.firstEntry();

            if (!oldest.getKey().isBefore(cutoff)) {
                break;
            }

            List<Transaction> expiredTransactions =
                    transactions.pollFirstEntry().getValue();

            for (Transaction transaction : expiredTransactions) {
                total = total.subtract(transaction.amount());
                transactionIds.remove(transaction.id());
            }
        }
    }

    public record Transaction(
            String id,
            BigDecimal amount,
            Instant timestamp
    ) {
    }
}