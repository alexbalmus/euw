package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.Role;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Source account role wrapper
 */
interface Account_Source extends Role<Account>
{
    String INSUFFICIENT_FUNDS = "Insufficient funds.";

    default void transfer(final Double amount, final Account_Destination destination)
    {
        if (unwrap().getBalance() < amount)
        {
            throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
        }
        unwrap().decreaseBalanceBy(amount);
        destination.receive(amount);
    }
}
