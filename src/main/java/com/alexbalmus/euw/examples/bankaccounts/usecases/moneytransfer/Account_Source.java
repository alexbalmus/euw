package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.RoleWrapper;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Source account role wrapper
 */
interface Account_Source<A extends Account> extends RoleWrapper<A>
{
    String INSUFFICIENT_FUNDS = "Insufficient funds.";

    default void transfer(final Double amount, final Account_Destination<? super A> destination)
    {
        if (unwrap().getBalance() < amount)
        {
            throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
        }
        unwrap().decreaseBalanceBy(amount);
        destination.receive(amount);
    }
}
