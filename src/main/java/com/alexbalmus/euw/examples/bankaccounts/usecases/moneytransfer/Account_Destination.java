package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.Role;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Destination account role wrapper
 */
interface Account_Destination extends Role<Account>
{
    default void receive(final Double amount)
    {
        unwrap().deposit(amount);
    }
}
