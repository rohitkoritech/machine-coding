package com.interview.lld.moneytransfer;

import java.math.BigDecimal;

public class Account {

    private BigDecimal balance;
    private boolean active;

    public Account(BigDecimal balance) {
        this.balance = balance;
        this.active = true;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}