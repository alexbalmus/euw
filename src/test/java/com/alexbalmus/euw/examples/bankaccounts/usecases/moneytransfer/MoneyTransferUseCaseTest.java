package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.testng.annotations.Test;

import static com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole.wrap;
import static org.testng.Assert.*;

@Test
public class MoneyTransferUseCaseTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(1L, 100.0);

        var destination = new SpecialAccount(2L, 200.0);

        var moneyTransferUseCase = new MoneyTransferUseCase();

        moneyTransferUseCase.transferFromSourceToDestination(source, destination, 50.0);

        assertEquals(source.getBalance(), 50.0);
        assertEquals(destination.getBalance(), 250.0);
    }

    @Test
    public void testExecuteInsufficientFunds()
    {
        var source = new Account(1L, 20.0);

        var destination = new Account(1L, 200.0);

        var moneyTransferUseCase = new MoneyTransferUseCase();

        try
        {
            moneyTransferUseCase.transferFromSourceToDestination(source, destination, 50.0);
            fail("Exception should have been thrown.");
        }
        catch (RuntimeException e)
        {
            assertEquals(e.getMessage(), Account.INSUFFICIENT_FUNDS);
        }
    }

    @Test
    public void testIdentity()
    {
        var account = new Account(1L, 20.0);

        var accountWrapper = wrap(account);

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
        public SpecialAccount(Long id, Double balance)
        {
            super(id, balance);
        }
    }
}
