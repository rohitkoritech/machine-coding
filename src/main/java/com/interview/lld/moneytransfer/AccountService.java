package com.interview.lld.moneytransfer;

import java.util.concurrent.ConcurrentHashMap;

public class AccountService {

    private final ConcurrentHashMap<String, Account> accounts =
            new ConcurrentHashMap<>();

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public void addAccount(String accountId, Account account) {
        accounts.put(accountId, account);
    }
}