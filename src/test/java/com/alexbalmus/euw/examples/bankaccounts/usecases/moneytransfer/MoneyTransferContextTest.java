package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class MoneyTransferContextTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(100.0);
        source.setId(1L);

        var destination = new SpecialAccount(200.0);
        destination.setId(2L);

        var moneyTransferContext = new MoneyTransferContext<>(50.0, source, destination);

        moneyTransferContext.executeSourceToDestinationTransfer();

        assertEquals(source.getBalance(), 50.0);
        assertEquals(destination.getBalance(), 250.0);
    }

    @Test
    public void testExecuteInsufficientFunds()
    {
        var source = new Account(20.0);
        source.setId(1L);

        var destination = new Account(200.0);
        destination.setId(2L);

        var moneyTransferContext = new MoneyTransferContext<>(50.0, source, destination);

        try
        {
            moneyTransferContext.executeSourceToDestinationTransfer();
            fail("Exception should have been thrown.");
        }
        catch (RuntimeException e)
        {
            assertEquals(e.getMessage(), MoneyTransferContext.Account_SourceRoleWrapper.INSUFFICIENT_FUNDS);
        }
    }

    @Test
    public void testIdentity()
    {
        var account1 = new Account(20.0);
        account1.setId(1L);

        var moneyTransferContext = new MoneyTransferContext<>(null, null, null);
        var sourceAccount = moneyTransferContext.wrapWithPotentialRoles(account1);

        assertEquals(sourceAccount.rolePlayer(), account1);
    }

    static class SpecialAccount extends Account
    {
        public SpecialAccount(Double balance)
        {
            super(balance);
        }
    }
}
