package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class MoneyTransferUseCaseTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(100.0);
        source.setId(1L);

        var destination = new SpecialAccount(200.0);
        destination.setId(2L);

        var moneyTransferUseCase = new MoneyTransferUseCase();

        moneyTransferUseCase.transferFromSourceToDestination(source, destination, 50.0);

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

        var moneyTransferUseCase = new MoneyTransferUseCase();

        try
        {
            moneyTransferUseCase.transferFromSourceToDestination(source, destination, 50.0);
            fail("Exception should have been thrown.");
        }
        catch (RuntimeException e)
        {
            assertEquals(e.getMessage(), Account_Source.INSUFFICIENT_FUNDS);
        }
    }

    @Test
    public void testIdentity()
    {
        var account = new Account(20.0);
        account.setId(1L);

        var accountWrapper = MoneyTransferUseCase.wrapWithPotentialRoles(account);

        assertEquals(accountWrapper.unwrap(), account);

        Account_Destination previousDestination = accountWrapper.assignRole();
        Account_Source currentSource = accountWrapper.assignRole();

        assertEquals(previousDestination, currentSource);
        assertEquals(previousDestination.unwrap(), currentSource.unwrap());
        assertEquals(previousDestination.unwrap(), account);
        assertEquals(currentSource.unwrap(), account);
    }

    static class SpecialAccount extends Account
    {
        public SpecialAccount(Double balance)
        {
            super(balance);
        }
    }
}
