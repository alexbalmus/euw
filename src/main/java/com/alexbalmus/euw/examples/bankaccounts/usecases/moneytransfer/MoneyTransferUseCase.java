package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import static com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole.wrap;

import com.alexbalmus.euw.common.Multirole;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

public class MoneyTransferUseCase
{
    private final Multirole<Account> wSource;
    private final Multirole<Account> wDestination;

    public MoneyTransferUseCase(Account source, Account destination)
    {
        wSource      = wrap(source);
        wDestination = wrap(destination);
    }

    /**
     * Transfer amount from source to destination
     * @param amount the amount to transfer
     */
    public void transferFromSourceToDestination(final Double amount)
    {
        //--- Use case roles setup:
        var rSource      = wSource.assignRole(Account_Source.class);
        var rDestination = wDestination.assignRole(Account_Destination.class);

        //--- Interaction:
        rSource.transfer(amount, rDestination);
    }

}
