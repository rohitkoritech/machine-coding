package com.interview.lld.aggregator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RollingTransactionAggregatorTest {

    private static final Instant NOW =
            Instant.parse("2026-09-26T10:00:00Z");

    private final Clock clock =
            Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void shouldCalculateTotalForTransactionsWithin24Hours() {

        RollingTransactionAggregator aggregator =
                new RollingTransactionAggregator(clock);

        aggregator.record(transaction(
                "tx-1",
                "100",
                NOW.minus(Duration.ofHours(2))
        ));

        aggregator.record(transaction(
                "tx-2",
                "200",
                NOW.minus(Duration.ofHours(5))
        ));

        assertEquals(
                new BigDecimal("300"),
                aggregator.getTotal()
        );
    }

    @Test
    void shouldExcludeTransactionsOlderThan24Hours() {

        RollingTransactionAggregator aggregator =
                new RollingTransactionAggregator(clock);

        aggregator.record(transaction(
                "tx-old",
                "100",
                NOW.minus(Duration.ofHours(25))
        ));

        aggregator.record(transaction(
                "tx-new",
                "200",
                NOW.minus(Duration.ofHours(2))
        ));

        assertEquals(
                new BigDecimal("200"),
                aggregator.getTotal()
        );
    }

    @Test
    void shouldCalculateCorrectTotalForMultipleTransactions() {

        RollingTransactionAggregator aggregator =
                new RollingTransactionAggregator(clock);

        aggregator.record(transaction("tx-1", "100", NOW.minus(Duration.ofHours(1))));
        aggregator.record(transaction("tx-2", "250", NOW.minus(Duration.ofHours(2))));
        aggregator.record(transaction("tx-3", "50", NOW.minus(Duration.ofHours(3))));

        assertEquals(
                new BigDecimal("400"),
                aggregator.getTotal()
        );
    }

    @Test
    void shouldHandleOutOfOrderTransactions() {

        RollingTransactionAggregator aggregator =
                new RollingTransactionAggregator(clock);

        // Deliberately recorded out of timestamp order.
        aggregator.record(transaction(
                "tx-3",
                "50",
                NOW.minus(Duration.ofHours(1)
                )));

        aggregator.record(transaction(
                "tx-1",
                "100",
                NOW.minus(Duration.ofHours(10)
                )));

        aggregator.record(transaction(
                "tx-2",
                "200",
                NOW.minus(Duration.ofHours(5)
                )));

        assertEquals(
                new BigDecimal("350"),
                aggregator.getTotal()
        );
    }

    @Test
    void shouldIgnoreDuplicateTransaction() {

        RollingTransactionAggregator aggregator =
                new RollingTransactionAggregator(clock);

        RollingTransactionAggregator.Transaction transaction =
                transaction(
                        "tx-1",
                        "100",
                        NOW.minus(Duration.ofHours(2)
                        ));

        aggregator.record(transaction);
        aggregator.record(transaction);

        assertEquals(
                new BigDecimal("100"),
                aggregator.getTotal()
        );
    }

    private RollingTransactionAggregator.Transaction transaction(
            String id,
            String amount,
            Instant timestamp
    ) {
        return new RollingTransactionAggregator.Transaction(
                id,
                new BigDecimal(amount),
                timestamp
        );
    }
}