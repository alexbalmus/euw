package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.Role;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Source account role wrapper
 */
interface Account_Source extends Role<Account>
{
    default void transfer(final Double amount, final Account_Destination destination)
    {
        if (unwrap() == destination.unwrap())
        {
            throw new IllegalArgumentException("Source and destination can't be the same.");
        }

        unwrap().withdraw(amount);
        destination.receive(amount);
    }
}
