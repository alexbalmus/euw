package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

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
    static Multirole<Account> wrapWithPotentialRoles(final Account account)
    {
        return (Account_Multirole) () -> account;
    }

    /**
     * Convenience method for creating a map of wrappers instead of calling wrapWithPotentialRoles(...) multiple times
     *
     * @param accountIds the entities to be wrapped
     *
     * @return the map of wrappers
     */
    final Map<Account, Multirole<Account>> createWrappersMap(final Account... accountIds)
    {
        var wrappersMap = new HashMap<Account, Multirole<Account>>();

        for (var account : accountIds)
        {
            wrappersMap.put(account, wrapWithPotentialRoles(account));
        }

        return wrappersMap;
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
        var wrappersMap = createWrappersMap(source, destination);

        transferMoney(
            wrappersMap.get(source),
            wrappersMap.get(destination),
            null,
            amount);
    }

    /**
     * Transfer amount from source to destination while traversing a temporary account
     * @param source the source account
     * @param destination the destination account
     * @param temp the temporary account
     * @param amount the amount to transfer
     */
    public void transferFromSourceToDestinationViaTemporary(
        final Account source, final Account destination, final Account temp, final Double amount)
    {
        var wrappersMap = createWrappersMap(source, destination, temp);

        transferMoney(
            wrappersMap.get(source),
            wrappersMap.get(temp),
            null, // previous destination
            amount);

        transferMoney(
            wrappersMap.get(temp),
            wrappersMap.get(destination),
            wrappersMap.get(temp), // previous destination
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
        Account_Source source = wSource.assignRole();
        Account_Destination destination = wDestination.assignRole();

        if (wPreviousDestination != null)
        {
            Account_Destination previousDestination = wPreviousDestination.assignRole();

            // Identity check: it's the same wrapper even though different roles were played in different installments:
            Validate.isTrue(source == previousDestination,
                "Source must match previous destination in this step of A-B-C transfer scenario.");
            // Likewise, it's the same underlying (wrapped) object:
            Validate.isTrue(source.unwrap() == previousDestination.unwrap());
        }

        //--- Interaction:
        source.transfer(amount, destination);
    }
}
