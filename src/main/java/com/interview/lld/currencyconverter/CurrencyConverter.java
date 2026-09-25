package com.interview.lld.currencyconverter;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CurrencyConverter {

    private final RateProvider rateProvider;
    private final Clock clock;
    private final long ttlMillis;

    // Actual cached rates.
    private final ConcurrentMap<String, CachedRate> cache =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompletableFuture<CachedRate>>
            inFlight = new ConcurrentHashMap<>();

    public CurrencyConverter(
            RateProvider rateProvider,
            Clock clock,
            long ttlMillis) {

        this.rateProvider = rateProvider;
        this.clock = clock;
        this.ttlMillis = ttlMillis;
    }

    public BigDecimal convert(
            BigDecimal amount,
            Currency from,
            Currency to) {

        if (from.equals(to)) {
            return amount;
        }

        String key = createKey(from, to);

        // Fast path: valid cached value.
        CachedRate cached = cache.get(key);

        if (cached != null && !cached.isExpired(clock, ttlMillis)) {
            return amount.multiply(cached.rate());
        }

        // Cache miss or expired.
        CachedRate fresh = getOrRefresh(key, from, to);

        return amount.multiply(fresh.rate());
    }

    private CachedRate getOrRefresh(
            String key,
            Currency from,
            Currency to) {

        CompletableFuture<CachedRate> newFuture =
                new CompletableFuture<>();

        CompletableFuture<CachedRate> existingFuture =
                inFlight.putIfAbsent(key, newFuture);

        if (existingFuture != null) {
            return existingFuture.join();
        }

        try {
            BigDecimal rate =
                    rateProvider.fetchRate(from, to);
            CachedRate fresh =
                    new CachedRate(
                            rate,
                            clock.millis()
                    );
            cache.put(key, fresh);
            newFuture.complete(fresh);
            return fresh;

        } catch (Exception e) {
            newFuture.completeExceptionally(e);
            throw e;

        } finally {
            inFlight.remove(key, newFuture);
        }
    }

    private String createKey(
            Currency from,
            Currency to) {

        return from.getCurrencyCode()
                + "->"
                + to.getCurrencyCode();
    }

    private record CachedRate(
            BigDecimal rate,
            long timestamp) {

        boolean isExpired(
                Clock clock,
                long ttlMillis) {
            return clock.millis() - timestamp >= ttlMillis;
        }
    }
}