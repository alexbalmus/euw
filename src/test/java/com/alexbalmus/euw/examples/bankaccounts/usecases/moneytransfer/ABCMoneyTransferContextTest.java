package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class ABCMoneyTransferContextTest
{
    @Test
    public void testExecuteSourceToDestinationTransfer()
    {
        var source = new Account(100.0);
        source.setId(1L);

        var intermediary = new Account(0.0);
        intermediary.setId(2L);

        var destination = new Account(200.0);
        destination.setId(3L);

        var abcMoneyTransferContext = new MoneyTransferContext<>(50.0, source, destination, intermediary);

        abcMoneyTransferContext.executeSourceToIntermediaryToDestinationTransfer();

        assertEquals(source.getBalance(), 50.0);
        assertEquals(intermediary.getBalance(), 0.0);
        assertEquals(destination.getBalance(), 250.0);
    }

    @Test
    public void testIdentity()
    {
        var account1 = new Account(20.0);
        account1.setId(1L);

        var abcMoneyTransferContext = new MoneyTransferContext<>(null, null, null, null);
        var bidirectionalAccount = abcMoneyTransferContext.wrapWithPotentialRoles(account1);

        var moneyTransferContext = new MoneyTransferContext<>(null, null, null);
        var sourceRef = moneyTransferContext.wrapWithPotentialRoles(account1);
        var destRef = moneyTransferContext.wrapWithPotentialRoles(account1);

        assertEquals(bidirectionalAccount.rolePlayer(), sourceRef.rolePlayer());
        assertEquals(bidirectionalAccount.rolePlayer(), destRef.rolePlayer());
    }
}
