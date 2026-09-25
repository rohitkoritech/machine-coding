package com.interview.lld.currencyconverter;

import java.math.BigDecimal;
import java.util.Currency;

public interface RateProvider {
    BigDecimal fetchRate(Currency fromCurrency, Currency toCurrency);
}