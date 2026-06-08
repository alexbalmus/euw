package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

import static com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole.wrap;

public class MoneyTransferUseCase
{
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
        var rSource      = wrap(source).assignRole(Account_Source.class);
        var rDestination = wrap(destination).assignRole(Account_Destination.class);

        //--- Interaction:
        rSource.transfer(amount, rDestination);
    }

}
