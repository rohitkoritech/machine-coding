package com.interview.lld.currencyconverter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConverterTest {

    @Test
    void shouldConvertSuccessfully() {

        RateProvider rateProvider = (from, to) ->
                new BigDecimal("1.10");

        Clock clock = Clock.fixed(
                Instant.parse("2026-09-25T12:00:00Z"),
                ZoneOffset.UTC
        );

        CurrencyConverter converter =
                new CurrencyConverter(
                        rateProvider,
                        clock,
                        10_000
                );

        BigDecimal result = converter.convert(
                new BigDecimal("100"),
                Currency.getInstance("EUR"),
                Currency.getInstance("USD")
        );

        assertEquals(
                new BigDecimal("110.00"),
                result
        );
    }

    @Test
    void shouldUseCachedRateOnSecondRequest() {

        AtomicInteger providerCalls = new AtomicInteger();

        RateProvider rateProvider = (from, to) -> {
            providerCalls.incrementAndGet();
            return new BigDecimal("1.10");
        };

        Clock clock = Clock.fixed(
                Instant.parse("2026-09-25T12:00:00Z"),
                ZoneOffset.UTC
        );

        CurrencyConverter converter =
                new CurrencyConverter(
                        rateProvider,
                        clock,
                        10_000
                );

        BigDecimal firstResult = converter.convert(
                new BigDecimal("100"),
                Currency.getInstance("EUR"),
                Currency.getInstance("USD")
        );

        BigDecimal secondResult = converter.convert(
                new BigDecimal("200"),
                Currency.getInstance("EUR"),
                Currency.getInstance("USD")
        );

        assertEquals(
                new BigDecimal("110.00"),
                firstResult
        );

        assertEquals(
                new BigDecimal("220.00"),
                secondResult
        );

        // Provider should only be called once.
        assertEquals(1, providerCalls.get());
    }
}
