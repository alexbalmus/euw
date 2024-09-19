package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Destination account role wrapper
 */
interface Account_DestinationRoleWrapper<A extends Account> extends Account_BasicRoleWrapper<A>
{
    default void receive(final Double amount)
    {
        rolePlayer().increaseBalanceBy(amount);
    }
}
