package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import static org.testng.Assert.*;

import static com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole.wrap;

import org.testng.annotations.Test;

import com.alexbalmus.euw.common.Role;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

@Test
public class MoneyTransferUseCaseTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(1L, 100.0);

        var destination = new SpecialAccount(2L, 200.0);

        var moneyTransferUseCase = new MoneyTransferUseCase(source, destination);

        moneyTransferUseCase.transferFromSourceToDestination(50.0);

        assertEquals(source.getBalance(), 50.0);
        assertEquals(destination.getBalance(), 250.0);
    }

    @Test
    public void testExecuteInsufficientFunds()
    {
        var source = new Account(1L, 20.0);

        var destination = new Account(1L, 200.0);

        var moneyTransferUseCase = new MoneyTransferUseCase(source, destination);

        try
        {
            moneyTransferUseCase.transferFromSourceToDestination(50.0);
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

        var previousDestination = accountWrapper.assignRole(Account_Destination.class);
        var currentSource = accountWrapper.assignRole(Account_Source.class);

        assertEquals(previousDestination, currentSource);
        assertEquals(previousDestination.unwrap(), currentSource.unwrap());
        assertEquals(previousDestination.unwrap(), account);
        assertEquals(currentSource.unwrap(), account);
    }

    @Test
    public void testInvalidRoleAssignment()
    {
        var accountWrapper = wrap(new Account(1L, 20.0));

        var exception = expectThrows(
            IllegalStateException.class,
            () -> accountWrapper.assignRole(InvalidAccountRole.class));

        assertTrue(exception.getMessage().contains("Attempting to play an invalid role"));
    }

    @Test
    public void testRejectSameSourceAndDestination()
    {
        var account = new Account(1L, 100.0);
        var moneyTransferUseCase = new MoneyTransferUseCase(account, account);

        var exception = expectThrows(
            IllegalArgumentException.class,
            () -> moneyTransferUseCase.transferFromSourceToDestination(50.0));

        assertEquals(exception.getMessage(), "Source and destination can't be the same.");
    }

    @Test
    public void testRejectInvalidAmount()
    {
        var account = new Account(1L, 100.0);

        var exception = expectThrows(
            IllegalArgumentException.class,
            () -> account.withdraw(-50.0));

        assertEquals(exception.getMessage(), Account.INVALID_AMOUNT);
    }

    interface InvalidAccountRole extends Role<Account>
    {
    }

    static class SpecialAccount extends Account
    {
        public SpecialAccount(Long id, Double balance)
        {
            super(id, balance);
        }
    }
}
