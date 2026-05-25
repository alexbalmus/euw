package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class ABCMoneyTransferUseCaseTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(1L,100.0);
        var intermediary = new Account(2L, 0.0);
        var destination = new Account(3L, 200.0);

        var abcMoneyTransferUseCase = new MultiroleMoneyTransferUseCase();

        abcMoneyTransferUseCase.transferFromSourceToDestinationViaTemporary(source, destination, intermediary, 50.0);

        assertEquals(source.getBalance(), 50.0);
        assertEquals(intermediary.getBalance(), 0.0);
        assertEquals(destination.getBalance(), 250.0);
    }
}
