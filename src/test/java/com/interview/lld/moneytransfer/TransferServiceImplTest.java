package com.interview.lld.moneytransfer;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceImplTest {

    @Test
    void shouldTransferMoneySuccessfully() {
        AccountService accountService = new AccountService();

        Account rohit = new Account(new BigDecimal("500"));
        Account priya = new Account(new BigDecimal("0"));

        accountService.addAccount("rohit", rohit);
        accountService.addAccount("priya", priya);

        TransferService service = new TransferServiceImpl(accountService);

        TransferResult result =
                service.transfer(
                        "rohit",
                        "priya",
                        new BigDecimal("200")
                );

        assertTrue(result.success());
        assertNotNull(result.transferId());
        assertNull(result.errorCode());

        assertEquals(
                new BigDecimal("300"),
                rohit.getBalance()
        );

        assertEquals(
                new BigDecimal("200"),
                priya.getBalance()
        );
    }
}