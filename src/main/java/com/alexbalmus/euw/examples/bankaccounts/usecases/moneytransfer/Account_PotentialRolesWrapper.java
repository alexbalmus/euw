package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * An all-potential-roles wrapper combining source and destination roles
 */
public interface Account_PotentialRolesWrapper<A extends Account>
    extends Account_SourceRoleWrapper<A>, Account_DestinationRoleWrapper<A>
{
}
