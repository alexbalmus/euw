package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.MultiroleWrapper;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.apache.commons.lang3.Validate;

public class MoneyTransferContext<A extends Account>
{
    private final Double amount;

    private final MultiroleWrapper<A> sourceWrapper;
    private final MultiroleWrapper<A> destinationWrapper;
    private final MultiroleWrapper<A> intermediaryWrapper;

    public MoneyTransferContext(
        final Double amount,
        final A sourceAccount,
        final A destinationAccount)
    {
        this(amount, sourceAccount, destinationAccount, null);
    }

    public MoneyTransferContext(
        final Double amount,
        final A sourceAccount,
        final A destinationAccount,
        final A intermediaryAccount)
    {
        this.amount = amount;

        // Potential roles wrapping:
        this.sourceWrapper = wrapWithPotentialRoles(sourceAccount);
        Validate.isTrue(sourceAccount == sourceWrapper.unwrap());

        this.destinationWrapper = wrapWithPotentialRoles(destinationAccount);
        Validate.isTrue(destinationAccount == destinationWrapper.unwrap());

        this.intermediaryWrapper = intermediaryAccount != null
            ? wrapWithPotentialRoles(intermediaryAccount)
            : null;
        if (intermediaryWrapper != null)
        {
            Validate.isTrue(intermediaryAccount == intermediaryWrapper.unwrap());
        }
    }

    // Potential roles wrapping:
    Account_Multirole<A> wrapWithPotentialRoles(final A account)
    {
        return () -> account;
    }

    // Use case variations:

    public void executeSourceToDestinationTransfer()
    {
        transferMoney(sourceWrapper, destinationWrapper, null, amount);
    }

    public void executeSourceToIntermediaryToDestinationTransfer()
    {
        Validate.notNull(intermediaryWrapper, "intermediaryAccount must not be null.");

        transferMoney(sourceWrapper, intermediaryWrapper, null, amount);
        transferMoney(intermediaryWrapper, destinationWrapper, intermediaryWrapper, amount);
    }

    private void transferMoney(
        final MultiroleWrapper<A> sourceWrapper,
        final MultiroleWrapper<A> destinationWrapper,
        // The purpose of the following parameter is just to prove that
        // we can check that the same object wrapper has played different roles in different installments:
        final MultiroleWrapper<A> previousDestinationWrapper,
        final Double amount)
    {
        Validate.isTrue(sourceWrapper != destinationWrapper,
            "Source and destination can't be the same.");

        if (previousDestinationWrapper != null)
        {
            Validate.isTrue(sourceWrapper == previousDestinationWrapper,
                "Source must match previous destination in this step of A-B-C transfer scenario.");
        }

        // Use case roles setup:
        final Account_Source<A> SOURCE = sourceWrapper.assignRole();
        final Account_Destination<A> DESTINATION = destinationWrapper.assignRole();

        // Interaction:
        SOURCE.transfer(amount, DESTINATION);
    }
}
