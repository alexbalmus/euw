package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.MultiroleWrapper;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * An all-potential-roles wrapper combining source and destination roles
 */
public interface Account_Multirole<A extends Account>
    extends MultiroleWrapper<A>, Account_Source<A>, Account_Destination<A>
{
}
