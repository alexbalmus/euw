package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.Multirole;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

public class MoneyTransferUseCase
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

    // Use case variations:

    /**
     * Transfer amount from source to destination
     * @param source the source account
     * @param destination the destination account
     * @param amount the amount to transfer
     */
    public void transferFromSourceToDestination(
        final Account source, final Account destination, final Double amount)
    {
        //--- Use case roles setup:
        Account_Source      wSource      = wrap(source).assignRole();
        Account_Destination wDestination = wrap(destination).assignRole();

        //--- Interaction:
        wSource.transfer(amount, wDestination);
    }

}
