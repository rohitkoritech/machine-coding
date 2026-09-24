package com.interview.lld.currencyconverter;

import java.math.BigDecimal;

public interface RateProvider {
    BigDecimal getRate(String fromCurrency, String toCurrency);
}