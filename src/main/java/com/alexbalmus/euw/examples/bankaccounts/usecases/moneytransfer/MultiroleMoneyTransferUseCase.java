package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import static com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole.wrap;

import org.apache.commons.lang3.Validate;

import com.alexbalmus.euw.common.Multirole;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;


public class MultiroleMoneyTransferUseCase
{
    private final Multirole<Account> wSource;
    private final Multirole<Account> wDestination;
    private final Multirole<Account> wTemp;

    public MultiroleMoneyTransferUseCase(final Account source, final Account destination, final Account temp)
    {
        wSource      = wrap(source);
        wDestination = wrap(destination);
        wTemp        = wrap(temp);
    }

    /**
     * Transfer amount from source to destination while traversing a temporary account
     * @param amount the amount to transfer
     */
    public void transferFromSourceToDestinationViaTemporary(final Double amount)
    {
        transferMoney(
            wSource,
            wTemp,
            null, // previous destination
            amount);

        transferMoney(
            wTemp,
            wDestination,
            wTemp, // previous destination
            amount);
    }

    /**
     * The parametrized use case method that performs the setup of necessary roles and kicks off the interaction
     *
     * @param wSource the source wrapper
     * @param wDestination the destination wrapper
     * @param wPreviousDestination the previous destination wrapper
     * @param amount the amount to transfer
     */
    private void transferMoney(
        final Multirole<Account> wSource,
        final Multirole<Account> wDestination,
        final Multirole<Account> wPreviousDestination,
        final Double amount)
    {
        Validate.isTrue(wSource != wDestination,
            "Source and destination can't be the same.");

        //--- Use case roles setup:
        Account_Source      rSource      = wSource.assignRole(Account_Source.class);
        Account_Destination rDestination = wDestination.assignRole(Account_Destination.class);

        if (wPreviousDestination != null)
        {
            Account_Destination rPreviousDestination =
                wPreviousDestination.assignRole(Account_Destination.class);

            // Identity check: it's the same wrapper even though different roles were played in different installments:
            Validate.isTrue(rSource == rPreviousDestination,
                "Source must match previous destination in this step of A-B-C transfer scenario.");

            // Likewise, it's the same underlying (wrapped) object:
            Validate.isTrue(rSource.unwrap() == rPreviousDestination.unwrap());
        }

        //--- Interaction:
        rSource.transfer(amount, rDestination);
    }
}
