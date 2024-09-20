package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.RoleWrapper;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Source account role wrapper
 */
interface Account_SourceRoleWrapper<A extends Account> extends RoleWrapper<A>
{
    String INSUFFICIENT_FUNDS = "Insufficient funds.";

    default void transfer(final Double amount, final Account_DestinationRoleWrapper<? super A> destination)
    {
        if (rolePlayer().getBalance() < amount)
        {
            throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
        }
        rolePlayer().decreaseBalanceBy(amount);
        destination.receive(amount);
    }
}
