package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.Multirole;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * An all-potential-roles wrapper combining source and destination roles
 */
interface Account_Multirole
    extends Multirole<Account>, Account_Source, Account_Destination
{
    /**
     * Static method for wrapping an entity with a multirole wrapper
     *
     * @param account the entity to wrap
     *
     * @return a multirole wrapper for the entity
     */
    static Multirole<Account> wrap(final Account account)
    {
        return (Account_Multirole) () -> account;
    }
}
